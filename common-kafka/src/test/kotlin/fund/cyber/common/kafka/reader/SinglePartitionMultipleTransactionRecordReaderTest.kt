package fund.cyber.common.kafka.reader

import fund.cyber.common.kafka.BaseForKafkaIntegrationTest
import fund.cyber.common.kafka.SinglePartitionTopicDataPresentLatch
import fund.cyber.common.kafka.sendRecordsInTransaction
import org.junit.jupiter.api.*
import org.springframework.kafka.test.context.EmbeddedKafka

const val MULTIPLE_TRANSACTION_RECORD_TOPIC = "MULTIPLE_TRANSACTION_RECORD_TOPIC"

@EmbeddedKafka(
        partitions = 1, topics = [MULTIPLE_TRANSACTION_RECORD_TOPIC],
        brokerProperties = [
            "auto.create.topics.enable=false", "transaction.state.log.replication.factor=1",
            "transaction.state.log.min.isr=1"
        ]
)
@DisplayName("Single-partitioned topic last items reader test")
class SinglePartitionMultipleTransactionRecordReaderTest : BaseForKafkaIntegrationTest() {

    private val itemsCount = 4


    @BeforeEach
    fun produceRecords() {

        val records = (0 until itemsCount).map { Pair("key", it) }
        sendRecordsInTransaction(embeddedKafka.brokersAsString, MULTIPLE_TRANSACTION_RECORD_TOPIC, records)

        SinglePartitionTopicDataPresentLatch(
                embeddedKafka.brokersAsString, MULTIPLE_TRANSACTION_RECORD_TOPIC, String::class.java, Int::class.java
        ).await()
    }


    @Test
    @DisplayName("Test topic with transaction returns required number of records")
    fun testMultipleTransactionRecords() {

        val reader = SinglePartitionTopicLastItemsReader(
                kafkaBrokers = embeddedKafka.brokersAsString, topic = MULTIPLE_TRANSACTION_RECORD_TOPIC,
                keyClass = String::class.java, valueClass = Int::class.java
        )
        val records = reader.readLastRecords(itemsCount)

        Assertions.assertEquals(4, records.size)
        (0 until itemsCount).forEach { Assertions.assertEquals(itemsCount - it - 1, records[it].second) }
    }
}