package fund.cyber

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration


@SpringBootApplication(exclude = [CassandraDataAutoConfiguration::class, KafkaAutoConfiguration::class])
class EthereumDumpApplication {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication(EthereumDumpApplication::class.java).run(*args)
        }
    }
}
