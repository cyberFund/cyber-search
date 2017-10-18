package fund.cyber.index.bitcoin.converter

import fund.cyber.index.btcd.BtcdBlock
import fund.cyber.node.common.sumByBigDecimal
import fund.cyber.node.model.BitcoinBlock
import fund.cyber.node.model.BitcoinBlockTransaction
import fund.cyber.node.model.BitcoinTransaction
import fund.cyber.node.model.BitcoinTransactionPreviewIO
import java.time.Instant

class BitcoinBlockConverter {

    fun btcdBlockToDao(btcdBlock: BtcdBlock, transactions: List<BitcoinTransaction>): BitcoinBlock {

        val blockTransactionsPreview = transactions
                .map { tx ->
                    BitcoinBlockTransaction(
                            fee = tx.fee, hash = tx.txid,
                            ins = tx.ins.map { input ->
                                BitcoinTransactionPreviewIO(addresses = input.addresses, amount = input.amount)
                            },
                            outs = tx.outs.map { out ->
                                BitcoinTransactionPreviewIO(addresses = out.addresses, amount = out.amount)
                            }
                    )
                }

        val totalOutputsValue = blockTransactionsPreview
                .flatMap { tx -> tx.outs }
                .sumByBigDecimal { out -> out.amount }

        return BitcoinBlock(
                hash = btcdBlock.hash, size = btcdBlock.size, version = btcdBlock.version, bits = btcdBlock.bits,
                difficulty = btcdBlock.difficulty.toBigInteger(), nonce = btcdBlock.nonce,
                time = Instant.ofEpochSecond(btcdBlock.time).toString(), weight = btcdBlock.weight,
                merkleroot = btcdBlock.merkleroot, height = btcdBlock.height, txs = blockTransactionsPreview,
                tx_number = blockTransactionsPreview.size, total_outputs_value = totalOutputsValue.toString()
        )
    }
}