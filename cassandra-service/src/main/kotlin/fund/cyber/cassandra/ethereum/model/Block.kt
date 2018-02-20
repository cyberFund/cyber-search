package fund.cyber.cassandra.ethereum.model

import fund.cyber.search.model.ethereum.EthereumBlock
import fund.cyber.search.model.ethereum.EthereumTransaction
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.math.BigInteger
import java.time.Instant

interface CqlEthereumItem

@Table("block")
data class CqlEthereumBlock(
        @PrimaryKey val number: Long,
        val hash: String,
        val parent_hash: String,
        val timestamp: Instant,
        val sha3_uncles: String,
        val logs_bloom: String,
        val transactions_root: String,
        val state_root: String,
        val receipts_root: String,
        val miner: String,
        val difficulty: BigInteger,
        val total_difficulty: BigInteger,
        val extra_data: String,
        val size: Long,
        val gas_limit: Long,
        val gas_used: Long,
        val tx_number: Int,
        val uncles: List<String>,
        val block_reward: String,
        val uncles_reward: String,
        val tx_fees: String
) : CqlEthereumItem {

    constructor(block: EthereumBlock) : this(
            number = block.number, hash = block.hash, parent_hash = block.parent_hash, timestamp = block.timestamp,
            sha3_uncles = block.sha3_uncles, logs_bloom = block.logs_bloom, transactions_root = block.transactions_root,
            state_root = block.state_root, receipts_root = block.receipts_root, miner = block.miner,
            difficulty = block.difficulty, total_difficulty = block.total_difficulty, extra_data = block.extra_data,
            size = block.size, gas_limit = block.gas_limit, gas_used = block.gas_used, tx_number = block.tx_number,
            uncles = block.uncles, block_reward = block.block_reward.toString(),
            uncles_reward = block.uncles_reward.toString(), tx_fees = block.tx_fees.toString()
    )
}

@Table("tx_preview_by_block")
data class CqlEthereumBlockTxPreview(
        @PrimaryKey val block_number: Long,
        val fee: String,
        val value: String,
        val hash: String,
        @Column(forceQuote = true) val from: String,
        @Column(forceQuote = true) val to: String,
        val creates_contract: Boolean
) : CqlEthereumItem {

    constructor(tx: EthereumTransaction) : this(
            block_number = tx.block_number, hash = tx.hash,
            fee = tx.fee.toString(), value = tx.value.toString(),
            from = tx.from, to = (tx.to ?: tx.creates)!!, //both 'to' or 'creates' can't be null at same time
            creates_contract = tx.creates != null
    )
}