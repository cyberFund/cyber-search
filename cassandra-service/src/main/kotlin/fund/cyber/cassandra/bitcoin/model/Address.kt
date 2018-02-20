package fund.cyber.cassandra.bitcoin.model

import org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED
import org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant

@Table("address_summary")
data class CqlBitcoinAddressSummary(

        @PrimaryKey val id: String,
        val confirmed_balance: String,
        val confirmed_total_received: BigDecimal,
        val confirmed_tx_number: Int,
        val kafka_delta_offset: Long,
        val kafka_delta_partition: Short,
        val kafka_delta_offset_committed: Boolean = false,
        val unconfirmed_tx_values: Map<String, BigDecimal> = emptyMap()
) : CqlBitcoinItem


@Table("tx_preview_by_address")
data class CqlBitcoinAddressTx(

        @PrimaryKeyColumn(ordinal = 0, type = PARTITIONED) val address: String,
        @PrimaryKeyColumn(ordinal = 1, type = CLUSTERED) val block_time: Instant,
        @PrimaryKeyColumn(ordinal = 2, type = CLUSTERED) val hash: String,
        val fee: BigDecimal,
        val block_number: Long,
        val ins: List<CqlBitcoinTxPreviewIO>,
        val outs: List<CqlBitcoinTxPreviewIO>
) : CqlBitcoinItem