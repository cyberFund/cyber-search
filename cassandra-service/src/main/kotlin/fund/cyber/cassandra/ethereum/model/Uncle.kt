package fund.cyber.cassandra.ethereum.model

import fund.cyber.search.model.ethereum.EthereumUncle
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant


@Table("uncle")
data class CqlEthereumUncle(
        @PrimaryKey val hash: String,
        val position: Int,
        val number: Long,
        val timestamp: Instant,
        val block_number: Long,
        val block_time: Instant,
        val block_hash: String,
        val miner: String,
        val uncle_reward: String
) : CqlEthereumItem {

    constructor(uncle: EthereumUncle) : this(
            hash = uncle.hash, position = uncle.position, number = uncle.number, timestamp = uncle.timestamp,
            block_number = uncle.block_number, block_time = uncle.block_time, block_hash = uncle.block_hash,
            miner = uncle.miner, uncle_reward = uncle.uncle_reward

    )
}

