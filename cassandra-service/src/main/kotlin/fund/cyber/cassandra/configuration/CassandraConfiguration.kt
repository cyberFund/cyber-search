package fund.cyber.cassandra.configuration

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.HostDistance
import com.datastax.driver.core.PoolingOptions
import fund.cyber.cassandra.common.defaultKeyspaceSpecification
import fund.cyber.cassandra.migration.DefaultMigrationsLoader
import fund.cyber.search.model.chains.Chain
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.message.BasicHeader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.support.GenericApplicationContext
import org.springframework.data.cassandra.ReactiveSession
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration
import org.springframework.data.cassandra.config.CassandraEntityClassScanner
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean
import org.springframework.data.cassandra.config.SchemaAction
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext
import org.springframework.data.cassandra.core.mapping.SimpleUserTypeResolver


const val MAX_CONCURRENT_REQUESTS = 8182
const val MAX_PER_ROUTE = 16
const val MAX_TOTAL = 32

val Chain.keyspace: String get() = lowerCaseName

const val REPOSITORY_NAME_DELIMETER = "__"

fun mappingContext(cluster: Cluster, keyspace: String): CassandraMappingContext {

    val mappingContext = CassandraMappingContext()

    mappingContext.setInitialEntitySet(CassandraEntityClassScanner.scan("fund.cyber.cassandra.bitcoin.model"))
    mappingContext.setUserTypeResolver(SimpleUserTypeResolver(cluster, keyspace))

    return mappingContext
}

fun getKeyspaceSession(cluster: Cluster,
                       keyspace: String,
                       converter: MappingCassandraConverter) = CassandraSessionFactoryBean()
        .apply {
            setCluster(cluster)
            setConverter(converter)
            setKeyspaceName(keyspace)
            schemaAction = SchemaAction.NONE
        }

abstract class CassandraRepositoriesConfiguration(
        private val cassandraHosts: String,
        private val cassandraPort: Int
) : AbstractReactiveCassandraConfiguration() {

    override fun getPoolingOptions() = PoolingOptions()
            .setMaxRequestsPerConnection(HostDistance.LOCAL, MAX_CONCURRENT_REQUESTS)
            .setMaxRequestsPerConnection(HostDistance.REMOTE, MAX_CONCURRENT_REQUESTS)!!

    override fun getPort() = cassandraPort
    override fun getContactPoints() = cassandraHosts

    @Bean
    @Primary
    override fun reactiveSession(): ReactiveSession {
        return super.reactiveSession()
    }

    override fun getKeyspaceCreations(): List<CreateKeyspaceSpecification> {
        return listOf(defaultKeyspaceSpecification("cyber_system"))
    }
}


@Configuration
class CassandraConfiguration {
    private val defaultHttpHeaders = listOf(BasicHeader("Keep-Alive", "timeout=10, max=1024"))
    private val connectionManager = PoolingHttpClientConnectionManager().apply {
        defaultMaxPerRoute = MAX_PER_ROUTE
        maxTotal = MAX_TOTAL
    }

    @Bean
    fun httpClient() = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setConnectionManagerShared(true)
            .setDefaultHeaders(defaultHttpHeaders)
            .build()!!

    @Bean
    fun migrationsLoader(resourceLoader: GenericApplicationContext) = DefaultMigrationsLoader(
            resourceLoader = resourceLoader
    )
}
