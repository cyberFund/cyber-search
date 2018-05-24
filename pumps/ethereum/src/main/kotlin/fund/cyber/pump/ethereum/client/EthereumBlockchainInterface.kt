package fund.cyber.pump.ethereum.client

import fund.cyber.pump.common.node.BlockBundle
import fund.cyber.pump.common.node.BlockchainInterface
import fund.cyber.pump.ethereum.client.genesis.EthereumGenesisDataProvider
import fund.cyber.common.await
import fund.cyber.pump.common.pool.PoolInterface
import fund.cyber.search.model.chains.ChainEntity
import fund.cyber.search.model.chains.ChainEntityType
import fund.cyber.search.model.ethereum.EthereumBlock
import fund.cyber.search.model.ethereum.EthereumTx
import fund.cyber.search.model.ethereum.EthereumUncle
import io.micrometer.core.instrument.MeterRegistry
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import org.springframework.stereotype.Component
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.response.EthBlock
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.parity.Parity
import org.web3j.protocol.parity.methods.response.Trace
import rx.Observable
import java.math.BigInteger

class EthereumBlockBundle(
        override val hash: String,
        override val parentHash: String,
        override val number: Long,
        override val blockSize: Int,
        val block: EthereumBlock,
        val uncles: List<EthereumUncle>,
        val txes: List<EthereumTx>
) : BlockBundle {
    override fun entitiesByType(chainEntityType: ChainEntityType): List<ChainEntity> {
        return when(chainEntityType) {
            ChainEntityType.BLOCK -> listOf(block)
            ChainEntityType.TX -> txes
            ChainEntityType.UNCLE -> uncles
        }
    }
}


@Component
class EthereumBlockchainInterface(
        private val parityClient: Parity,
        private val parityToBundleConverter: ParityToEthereumBundleConverter,
        private val genesisDataProvider: EthereumGenesisDataProvider,
        monitoring: MeterRegistry
) : BlockchainInterface<EthereumBlockBundle>, PoolInterface<EthereumTx> {

    private val downloadSpeedMonitor = monitoring.timer("pump_bundle_download")

    override fun lastNetworkBlock() = parityClient.ethBlockNumber().send().blockNumber.longValueExact()

    override fun blockBundleByNumber(number: Long): EthereumBlockBundle {


        val bundleRawData = downloadSpeedMonitor.recordCallable { downloadBundleData(number) }

        val bundle = parityToBundleConverter.convert(bundleRawData)
        return if (number == 0L) genesisDataProvider.provide(bundle) else bundle
    }

    override fun subscribePool(): Flowable<EthereumTx> {
        return Flowable.create<EthereumTx>({ emitter ->

            parityClient
                .pendingTransactionObservable()
                .flatMap { e -> Observable.just(parityToBundleConverter.parityMempoolTxToDao(e)) }
                .subscribe( { v -> emitter.onNext(v) }, { e -> emitter.onError(e)}, { emitter.onComplete() })

        }, BackpressureStrategy.BUFFER)
    }

    private fun downloadBundleData(number: Long): BundleRawData {

        val blockParameter = blockParameter(number.toBigInteger())
        val ethBlock = parityClient.ethGetBlockByNumber(blockParameter, true).send()

        val uncles = downloadUnclesData(ethBlock)

        val txsReceipts = downloadTransactionReceiptData(ethBlock)

        val calls = parityClient.traceBlock(blockParameter).send().traces

        return BundleRawData(ethBlock.block, uncles, txsReceipts, calls)
    }

    private fun downloadUnclesData(ethBlock: EthBlock): List<EthBlock.Block> {
        val unclesFutures = ethBlock.block.uncles.mapIndexed { index, _ ->
            parityClient
                    .ethGetUncleByBlockHashAndIndex(ethBlock.block.hash, BigInteger.valueOf(index.toLong()))
                    .sendAsync()
        }
        return unclesFutures.await().map { uncleEthBlock -> uncleEthBlock.block }
    }

    private fun downloadTransactionReceiptData(ethBlock: EthBlock): List<TransactionReceipt> {
        val receiptFutures = ethBlock.block.transactions.filterIsInstance<EthBlock.TransactionObject>()
            .map { tx -> tx.hash }
            .map { txHash ->
                parityClient.ethGetTransactionReceipt(txHash).sendAsync()
            }

        return receiptFutures.await().map { receipt -> receipt.result }
    }

    private fun blockParameter(blockNumber: BigInteger) = DefaultBlockParameter.valueOf(blockNumber)!!
}

data class BundleRawData(
    val block: EthBlock.Block,
    val uncles: List<EthBlock.Block>,
    val txsReceipts: List<TransactionReceipt>,
    val calls: List<Trace>
)
