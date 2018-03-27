package fund.cyber.common.kafka.reader

import fund.cyber.common.kafka.BaseForKafkaIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.kafka.test.context.EmbeddedKafka


const val NON_EXISTING_TOPIC = "NON_EXISTING_TOPIC"
const val EXISTING_EMPTY_TOPIC = "EXISTING_EMPTY_TOPIC"

@EmbeddedKafka(topics = [EXISTING_EMPTY_TOPIC], partitions = 1)
@DisplayName("Single-partitioned topic without items reader tests")
class SinglePartitionNonRecordsReaderTest : BaseForKafkaIntegrationTest() {

    @Test
    @DisplayName("Test non-existing topic")
    fun testNonExistingTopic() {

        val reader = SinglePartitionTopicLastItemsReader(
                kafkaBrokers = embeddedKafka.brokersAsString, topic = NON_EXISTING_TOPIC,
                keyClass = String::class.java, valueClass = Int::class.java
        )

        val records = reader.readLastRecords(1)
        assertEquals(0, records.size)
    }

    @Test
    @DisplayName("Test existing empty topic")
    fun testEmptyTopic() {

        val reader = SinglePartitionTopicLastItemsReader(
                kafkaBrokers = embeddedKafka.brokersAsString, topic = EXISTING_EMPTY_TOPIC,
                keyClass = String::class.java, valueClass = Int::class.java
        )

        val records = reader.readLastRecords(1)
        assertEquals(0, records.size)
    }
}
