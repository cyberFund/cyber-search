package fund.cyber

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration
import org.springframework.boot.autoconfigure.data.cassandra.CassandraRepositoriesAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux


@EnableWebFlux
@SpringBootApplication(exclude = [CassandraDataAutoConfiguration::class, CassandraRepositoriesAutoConfiguration::class])
class SearchApiApplication


fun main(args: Array<String>) {
    runApplication<SearchApiApplication>(*args)
}
//todo everywhere 404 if object not found
