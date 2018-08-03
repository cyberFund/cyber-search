package fund.cyber.supply

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration

@SpringBootApplication(exclude = [CassandraDataAutoConfiguration::class, KafkaAutoConfiguration::class])
class BitcoinSupplyApplication {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication(BitcoinSupplyApplication::class.java).run(*args)
        }
    }
}
