package fund.cyber.dump.ethereum

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import fund.cyber.cassandra.ethereum.model.CqlEthereumAddressTxPreview
import fund.cyber.cassandra.ethereum.model.CqlEthereumBlockTxPreview
import fund.cyber.cassandra.ethereum.model.CqlEthereumTx
import fund.cyber.cassandra.ethereum.repository.EthereumAddressTxRepository
import fund.cyber.cassandra.ethereum.repository.EthereumBlockTxRepository
import fund.cyber.cassandra.ethereum.repository.EthereumTxRepository
import fund.cyber.search.model.chains.EthereumFamilyChain
import fund.cyber.search.model.ethereum.EthereumTx
import fund.cyber.search.model.events.PumpEvent
import fund.cyber.search.model.events.txPumpTopic
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.Instant

class TxDumpProcessTest {


    //        --- D --- E --- G --- I
    //A --- B --- C --- F --- H
    @Test
    @Suppress("LongMethod")
    fun testWithDroppedTxs() {

        val txC = EthereumTx(
                hash = "C",
                nonce = 0, blockHash = "C",
                blockNumber = 4959189, blockTime = Instant.now(), positionInBlock = 1,
                from = "a", to = "b",
                value = BigDecimal.ZERO, gasPrice = BigDecimal.ZERO, gasLimit = 0,
                gasUsed = 21000L, fee = BigDecimal.ZERO, input = "", createdContract = null
        )

        val txD = EthereumTx(
                hash = "D",
                nonce = 0, blockHash = "D",
                blockNumber = 4959189, blockTime = Instant.now(), positionInBlock = 1,
                from = "a", to = "b",
                value = BigDecimal.ZERO, gasPrice = BigDecimal.ZERO, gasLimit = 0,
                gasUsed = 21000L, fee = BigDecimal.ZERO, input = "", createdContract = null
        )

        val txE = EthereumTx(
                hash = "E",
                nonce = 0, blockHash = "E",
                blockNumber = 4959189, blockTime = Instant.now(), positionInBlock = 1,
                from = "a", to = "b",
                value = BigDecimal.ZERO, gasPrice = BigDecimal.ZERO, gasLimit = 0,
                gasUsed = 21000L, fee = BigDecimal.ZERO, input = "", createdContract = null
        )

        val txF = EthereumTx(
                hash = "F",
                nonce = 0, blockHash = "F",
                blockNumber = 4959189, blockTime = Instant.now(), positionInBlock = 1,
                from = "a", to = "b",
                value = BigDecimal.ZERO, gasPrice = BigDecimal.ZERO, gasLimit = 0,
                gasUsed = 21000L, fee = BigDecimal.ZERO, input = "", createdContract = null
        )

        val txG = EthereumTx(
                hash = "G",
                nonce = 0, blockHash = "G",
                blockNumber = 4959189, blockTime = Instant.now(), positionInBlock = 1,
                from = "a", to = "b",
                value = BigDecimal.ZERO, gasPrice = BigDecimal.ZERO, gasLimit = 0,
                gasUsed = 21000L, fee = BigDecimal.ZERO, input = "", createdContract = null
        )

        val txH = EthereumTx(
                hash = "H",
                nonce = 0, blockHash = "H",
                blockNumber = 4959189, blockTime = Instant.now(), positionInBlock = 1,
                from = "a", to = "b",
                value = BigDecimal.ZERO, gasPrice = BigDecimal.ZERO, gasLimit = 0,
                gasUsed = 21000L, fee = BigDecimal.ZERO, input = "", createdContract = null
        )

        val txI = EthereumTx(
                hash = "I",
                nonce = 0, blockHash = "I",
                blockNumber = 4959189, blockTime = Instant.now(), positionInBlock = 1,
                from = "a", to = "b",
                value = BigDecimal.ZERO, gasPrice = BigDecimal.ZERO, gasLimit = 0,
                gasUsed = 21000L, fee = BigDecimal.ZERO, input = "", createdContract = null
        )


        val record1 = ConsumerRecord<PumpEvent, EthereumTx>(EthereumFamilyChain.ETHEREUM.txPumpTopic, 0,
                0, PumpEvent.NEW_BLOCK, txH)
        val record2 = ConsumerRecord<PumpEvent, EthereumTx>(EthereumFamilyChain.ETHEREUM.txPumpTopic, 0,
                0, PumpEvent.DROPPED_BLOCK, txH)
        val record3 = ConsumerRecord<PumpEvent, EthereumTx>(EthereumFamilyChain.ETHEREUM.txPumpTopic, 0,
                0, PumpEvent.DROPPED_BLOCK, txF)
        val record4 = ConsumerRecord<PumpEvent, EthereumTx>(EthereumFamilyChain.ETHEREUM.txPumpTopic, 0,
                0, PumpEvent.DROPPED_BLOCK, txC)
        val record5 = ConsumerRecord<PumpEvent, EthereumTx>(EthereumFamilyChain.ETHEREUM.txPumpTopic, 0,
                0, PumpEvent.NEW_BLOCK, txD)
        val record6 = ConsumerRecord<PumpEvent, EthereumTx>(EthereumFamilyChain.ETHEREUM.txPumpTopic, 0,
                0, PumpEvent.NEW_BLOCK, txE)
        val record7 = ConsumerRecord<PumpEvent, EthereumTx>(EthereumFamilyChain.ETHEREUM.txPumpTopic, 0,
                0, PumpEvent.NEW_BLOCK, txG)
        val record8 = ConsumerRecord<PumpEvent, EthereumTx>(EthereumFamilyChain.ETHEREUM.txPumpTopic, 0,
                0, PumpEvent.NEW_BLOCK, txI)

        val txRepository = mock<EthereumTxRepository> {
            on { saveAll(any<Iterable<CqlEthereumTx>>()) }.thenReturn(Flux.empty())
            on { deleteAll(any<Iterable<CqlEthereumTx>>()) }.thenReturn(Mono.empty())
        }
        val blockTxRepository = mock<EthereumBlockTxRepository> {
            on { saveAll(any<Iterable<CqlEthereumBlockTxPreview>>()) }.thenReturn(Flux.empty())
            on { deleteAll(any<Iterable<CqlEthereumBlockTxPreview>>()) }.thenReturn(Mono.empty())
        }
        val addressTxRepository = mock<EthereumAddressTxRepository> {
            on { saveAll(any<Iterable<CqlEthereumAddressTxPreview>>()) }.thenReturn(Flux.empty())
            on { deleteAll(any<Iterable<CqlEthereumAddressTxPreview>>()) }.thenReturn(Mono.empty())
        }

        val txDumpProcess = TxDumpProcess(txRepository, blockTxRepository, addressTxRepository,
                EthereumFamilyChain.ETHEREUM, SimpleMeterRegistry())

        txDumpProcess.onMessage(listOf(record1, record2, record3, record4, record5, record6, record7, record8))


        verify(txRepository, times(1))
                .saveAll(listOf(CqlEthereumTx(txD), CqlEthereumTx(txE),
                        CqlEthereumTx(txG), CqlEthereumTx(txI)))
        verify(txRepository, times(1))
                .deleteAll(listOf(CqlEthereumTx(txF), CqlEthereumTx(txC)))

        verify(blockTxRepository, times(1))
                .saveAll(listOf(CqlEthereumBlockTxPreview(txD), CqlEthereumBlockTxPreview(txE),
                        CqlEthereumBlockTxPreview(txG), CqlEthereumBlockTxPreview(txI)))
        verify(blockTxRepository, times(1))
                .deleteAll(listOf(CqlEthereumBlockTxPreview(txF), CqlEthereumBlockTxPreview(txC)))

        verify(addressTxRepository, times(1))
                .saveAll(
                        listOf(txD, txE, txG, txI)
                                .flatMap { tx ->
                                    tx.addressesUsedInTransaction().map { it -> CqlEthereumAddressTxPreview(tx, it) }
                                }
                )
        verify(addressTxRepository, times(1))
                .deleteAll(
                        listOf(txF, txC)
                                .flatMap { tx ->
                                    tx.addressesUsedInTransaction().map { it -> CqlEthereumAddressTxPreview(tx, it) }
                                }
                )


    }


}
