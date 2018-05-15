package fund.cyber.search.configuration

const val DEV_ENVIRONMENT = "DEV_ENVIRONMENT"

const val CHAIN = "CHAIN"
const val CHAIN_FAMILY = "CHAIN_FAMILY"
const val CHAIN_NAME = "CHAIN_NAME"
const val CHAIN_NODE_URL = "CHAIN_NODE_URL"

const val KAFKA_BROKERS = "KAFKA_BROKERS"
const val KAFKA_BROKERS_DEFAULT = "localhost:9092"

const val KAFKA_TRANSACTION_BATCH = "KAFKA_TRANSACTION_BATCH"
const val KAFKA_TRANSACTION_BATCH_DEFAULT = 1

const val CASSANDRA_HOSTS = "CASSANDRA_HOSTS"
const val CASSANDRA_HOSTS_DEFAULT = "localhost"

const val CASSANDRA_PORT = "CASSANDRA_PORT"
const val CASSANDRA_PORT_DEFAULT = 9042

const val ELASTIC_HTTP_PORT = "ELASTIC_HTTP_PORT"
const val ELASTIC_HTTP_PORT_DEFAULT = 9200

const val ELASTIC_TRANSPORT_PORT = "ELASTIC_TRANSPORT_PORT"
const val ELASTIC_TRANSPORT_PORT_DEFAULT = 9300

const val ELASTIC_CLUSTER_NAME = "ELASTIC_CLUSTER_NAME"
const val ELASTIC_CLUSTER_NAME_DEFAULT = "CYBER_SEARCH"

const val CORS_ALLOWED_ORIGINS = "CORS_ALLOWED_ORIGINS"
const val CORS_ALLOWED_ORIGINS_DEFAULT = "search.fund.cyber.fund"

const val START_BLOCK_NUMBER = "START_BLOCK_NUMBER"

const val STACK_CACHE_SIZE = "STACK_CACHE_SIZE"
const val STACK_CACHE_SIZE_DEFAULT = 100

const val WITH_MEMPOOL = "WITH_MEMPOOL"

const val BITCOIN_TX_OUTS_CACHE_HEAP_SIZE = "BITCOIN_TX_OUTS_CACHE_HEAP_SIZE"
const val BITCOIN_TX_OUTS_CACHE_HEAP_SIZE_DEFAULT = 4


inline fun <reified T : Any> env(name: String, default: T): T =
        when (T::class) {
            String::class -> (System.getenv(name) ?: default) as T
            Boolean::class -> (System.getenv(name)?.toBoolean() ?: default) as T
            Int::class -> (System.getenv(name)?.toIntOrNull() ?: default) as T
            Long::class -> (System.getenv(name)?.toLongOrNull() ?: default) as T
            else -> default
        }
