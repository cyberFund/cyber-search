package fund.cyber.cassandra.ethereum.configuration

import com.datastax.driver.core.Cluster
import com.datastax.driver.extras.codecs.jdk8.InstantCodec
import fund.cyber.cassandra.common.NoChainCondition
import fund.cyber.cassandra.common.defaultKeyspaceSpecification
import fund.cyber.cassandra.configuration.CassandraRepositoriesConfiguration
import fund.cyber.cassandra.configuration.REPOSITORY_NAME_DELIMETER
import fund.cyber.cassandra.configuration.getKeyspaceSession
import fund.cyber.cassandra.configuration.keyspace
import fund.cyber.cassandra.configuration.mappingContext
import fund.cyber.cassandra.ethereum.repository.EthereumContractRepository
import fund.cyber.cassandra.ethereum.repository.EthereumBlockRepository
import fund.cyber.cassandra.ethereum.repository.EthereumTxRepository
import fund.cyber.cassandra.ethereum.repository.EthereumUncleRepository
import fund.cyber.cassandra.ethereum.repository.PageableEthereumContractMinedBlockRepository
import fund.cyber.cassandra.ethereum.repository.PageableEthereumContractMinedUncleRepository
import fund.cyber.cassandra.ethereum.repository.PageableEthereumContractTxRepository
import fund.cyber.cassandra.ethereum.repository.PageableEthereumBlockTxRepository
import fund.cyber.cassandra.migration.BlockchainMigrationSettings
import fund.cyber.cassandra.migration.MigrationSettings
import fund.cyber.search.configuration.CASSANDRA_HOSTS
import fund.cyber.search.configuration.CASSANDRA_HOSTS_DEFAULT
import fund.cyber.search.configuration.CASSANDRA_PORT
import fund.cyber.search.configuration.CASSANDRA_PORT_DEFAULT
import fund.cyber.search.configuration.CHAIN
import fund.cyber.search.configuration.env
import fund.cyber.search.model.chains.EthereumFamilyChain
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.data.cassandra.ReactiveSession
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean
import org.springframework.data.cassandra.config.ClusterBuilderConfigurer
import org.springframework.data.cassandra.core.CassandraTemplate
import org.springframework.data.cassandra.core.ReactiveCassandraOperations
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification
import org.springframework.data.cassandra.core.cql.session.DefaultBridgedReactiveSession
import org.springframework.data.cassandra.core.cql.session.DefaultReactiveSessionFactory
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories
import org.springframework.data.cassandra.repository.support.CassandraRepositoryFactory
import org.springframework.data.cassandra.repository.support.ReactiveCassandraRepositoryFactory
import org.springframework.stereotype.Component


@Configuration
@EnableReactiveCassandraRepositories(
        basePackages = ["fund.cyber.cassandra.ethereum.repository"],
        reactiveCassandraTemplateRef = "ethereumCassandraTemplate"
)
@Conditional(EthereumFamilyChainCondition::class)
class EthereumRepositoryConfiguration(
        @Value("\${$CASSANDRA_HOSTS:$CASSANDRA_HOSTS_DEFAULT}")
        private val cassandraHosts: String,
        @Value("\${$CASSANDRA_PORT:$CASSANDRA_PORT_DEFAULT}")
        private val cassandraPort: Int
) : CassandraRepositoriesConfiguration(cassandraHosts, cassandraPort) {

    private val chain = EthereumFamilyChain.valueOf(env(CHAIN, ""))

    override fun getKeyspaceName(): String = chain.keyspace
    override fun getEntityBasePackages(): Array<String> = arrayOf("fund.cyber.cassandra.ethereum.model")

    override fun getKeyspaceCreations(): List<CreateKeyspaceSpecification> {
        return super.getKeyspaceCreations() + listOf(defaultKeyspaceSpecification(chain.lowerCaseName))
    }

    @Bean
    fun migrationSettings(): MigrationSettings {
        return BlockchainMigrationSettings(chain)
    }

    @Bean("ethereumCassandraTemplate")
    fun reactiveCassandraTemplate(
            @Qualifier("ethereumReactiveSession") session: ReactiveSession
    ): ReactiveCassandraOperations {
        return ReactiveCassandraTemplate(DefaultReactiveSessionFactory(session), cassandraConverter())
    }

    @Bean("ethereumReactiveSession")
    fun reactiveSession(
            @Qualifier("ethereumSession") session: CassandraSessionFactoryBean
    ): ReactiveSession {
        return DefaultBridgedReactiveSession(session.`object`)
    }

    override fun getClusterBuilderConfigurer(): ClusterBuilderConfigurer? {
        return ClusterBuilderConfigurer { clusterBuilder ->
            clusterBuilder.configuration.codecRegistry.register(InstantCodec.instance)
            return@ClusterBuilderConfigurer clusterBuilder
        }
    }

    @Bean("ethereumSession")
    override fun session(): CassandraSessionFactoryBean {
        val session = super.session()
        session.setKeyspaceName(keyspaceName)
        return session
    }
}


private class EthereumFamilyChainCondition : Condition {

    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val chain = context.environment.getProperty(CHAIN) ?: ""
        return EthereumFamilyChain.values().map(EthereumFamilyChain::name).contains(chain)
    }
}


@Component("ethereum-cassandra-repositories")
@Conditional(NoChainCondition::class)
class EthereumRepositoriesConfiguration : InitializingBean {

    @Autowired
    private lateinit var applicationContext: GenericApplicationContext
    @Autowired
    private lateinit var cluster: Cluster

    override fun afterPropertiesSet() {
        registerEthereumRepositories()
    }

    fun registerEthereumRepositories() {

        val beanFactory = applicationContext.beanFactory

        cluster.metadata.keyspaces
                .filter { keyspace -> keyspace.name.startsWith("ethereum", true) }
                .forEach { keyspace ->

                    //create sessions
                    val converter = MappingCassandraConverter(mappingContext(cluster, keyspace.name))
                    val session = getKeyspaceSession(cluster, keyspace.name, converter).also { it.afterPropertiesSet() }
                    val reactiveSession = DefaultReactiveSessionFactory(DefaultBridgedReactiveSession(session.`object`))

                    // create cassandra operations
                    val reactiveCassandraOperations = ReactiveCassandraTemplate(reactiveSession, converter)
                    val cassandraOperations = CassandraTemplate(session.`object`, converter)

                    // create repository factories
                    val reactiveRepositoryFactory = ReactiveCassandraRepositoryFactory(reactiveCassandraOperations)
                    val repositoryFactory = CassandraRepositoryFactory(cassandraOperations)

                    // create repositories
                    val blockRepository = reactiveRepositoryFactory.getRepository(EthereumBlockRepository::class.java)
                    val blockTxRepository = repositoryFactory
                            .getRepository(PageableEthereumBlockTxRepository::class.java)

                    val txRepository = reactiveRepositoryFactory.getRepository(EthereumTxRepository::class.java)

                    val contractRepository = reactiveRepositoryFactory
                            .getRepository(EthereumContractRepository::class.java)
                    val contractTxRepository = repositoryFactory
                            .getRepository(PageableEthereumContractTxRepository::class.java)
                    val contractUncleRepository = repositoryFactory
                            .getRepository(PageableEthereumContractMinedUncleRepository::class.java)
                    val contractBlockRepository = repositoryFactory
                            .getRepository(PageableEthereumContractMinedBlockRepository::class.java)

                    val uncleRepository = reactiveRepositoryFactory.getRepository(EthereumUncleRepository::class.java)

                    val repositoryPrefix = "${keyspace.name}$REPOSITORY_NAME_DELIMETER"

                    // register repositories
                    beanFactory.registerSingleton("${repositoryPrefix}blockRepository", blockRepository)
                    beanFactory.registerSingleton("${repositoryPrefix}pageableBlockTxRepository",
                                    blockTxRepository)

                    beanFactory.registerSingleton("${repositoryPrefix}txRepository", txRepository)

                    beanFactory.registerSingleton("${repositoryPrefix}contractRepository", contractRepository)
                    beanFactory.registerSingleton("${repositoryPrefix}pageableContractTxRepository",
                            contractTxRepository)
                    beanFactory.registerSingleton("${repositoryPrefix}pageableContractBlockRepository",
                            contractBlockRepository)
                    beanFactory.registerSingleton("${repositoryPrefix}pageableContractUncleRepository",
                            contractUncleRepository)

                    beanFactory.registerSingleton("${repositoryPrefix}uncleRepository", uncleRepository)
                }
    }

}
