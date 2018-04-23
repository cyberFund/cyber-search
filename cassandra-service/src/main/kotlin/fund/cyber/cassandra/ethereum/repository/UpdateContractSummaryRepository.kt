package fund.cyber.cassandra.ethereum.repository

import com.datastax.driver.core.ConsistencyLevel
import fund.cyber.cassandra.ethereum.model.CqlEthereumContractSummary
import org.springframework.data.cassandra.repository.Consistency
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


//todo write small tests for query consistency with object fields.
/**
 * To archive atomic updates of contract summaries we should use CAS, two-phase commit, serial reads and quorum writes.
 */
interface EthereumUpdateContractSummaryRepository : ReactiveCrudRepository<CqlEthereumContractSummary, String> {

    @Consistency(value = ConsistencyLevel.LOCAL_QUORUM)
    fun findByHash(hash: String): Mono<CqlEthereumContractSummary>

    @Consistency(value = ConsistencyLevel.LOCAL_QUORUM)
    fun findAllByHash(hashes: Iterable<String>): Flux<CqlEthereumContractSummary> = Flux.fromIterable(hashes)
        .flatMap { hash -> findByHash(hash) }

    /**
     * Return {@code true} if update was successful.
     */
    @Consistency(value = ConsistencyLevel.LOCAL_QUORUM)
    @Query("""
        UPDATE contract_summary
        SET confirmed_balance = :#{#summary.confirmedBalance},
            smart_contract = :#{#summary.smartContract},
            confirmed_total_received = :#{#summary.confirmedTotalReceived},
            tx_number = :#{#summary.txNumber},
            uncle_number = :#{#summary.minedUncleNumber},
            mined_block_number = :#{#summary.minedBlockNumber},
            last_activity_date = :#{#summary.lastActivityDate},
            kafka_delta_offset = :#{#summary.kafkaDeltaOffset},
            kafka_delta_topic = :#{#summary.kafkaDeltaTopic},
            kafka_delta_partition = :#{#summary.kafkaDeltaPartition},
            version = :#{#summary.version},
            kafka_delta_offset_committed = false
        WHERE hash = :#{#summary.hash}
        IF version = :oldVersion
        """)
    fun update(@Param("summary") summary: CqlEthereumContractSummary,
               @Param("oldVersion") oldVersion: Long): Mono<Boolean>

    /**
     * Return {@code true} if there is no record for key and insert was successful.
     */
    @Consistency(value = ConsistencyLevel.LOCAL_QUORUM)
    @Query("""
        INSERT INTO contract_summary (hash, confirmed_balance, smart_contract,
          confirmed_total_received, tx_number, uncle_number, mined_block_number,
          first_activity_date, last_activity_date, version, kafka_delta_offset, kafka_delta_topic,
          kafka_delta_partition, kafka_delta_offset_committed)
        VALUES (:#{#summary.hash}, :#{#summary.confirmedBalance}, :#{#summary.smartContract},
            :#{#summary.confirmedTotalReceived}, :#{#summary.txNumber}, :#{#summary.minedUncleNumber},
            :#{#summary.minedBlockNumber}, :#{#summary.firstActivityDate}, :#{#summary.lastActivityDate},
            :#{#summary.version}, :#{#summary.kafkaDeltaOffset},
            :#{#summary.kafkaDeltaTopic}, :#{#summary.kafkaDeltaPartition},
            false)
        IF NOT EXISTS
        """)
    fun insertIfNotRecord(@Param("summary") summary: CqlEthereumContractSummary): Mono<Boolean>

    @Consistency(value = ConsistencyLevel.LOCAL_QUORUM)
    @Query("""
        UPDATE contract_summary
        SET kafka_delta_offset_committed = true,
            version = :newVersion
        WHERE hash = :contract
        """)
    fun commitUpdate(@Param("contract") contract: String, @Param("newVersion") newVersion: Long): Mono<Boolean>
}
