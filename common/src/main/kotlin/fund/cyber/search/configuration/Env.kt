package fund.cyber.search.configuration

const val CHAIN = "CHAIN"
const val CHAIN_NODE_URL = "CHAIN_NODE_URL"
const val BITCOIN_CHAIN_NODE_DEFAULT_URL = "http://cyber:cyber@127.0.0.1:8332"
const val ETHEREUM_CHAIN_NODE_DEFAULT_URL = "http://cyber:cyber@127.0.0.1:8545"

const val KAFKA_BROKERS = "KAFKA_BROKERS"
const val KAFKA_BROKERS_DEFAULT = "localhost:9092"

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


inline fun <reified T : Any> env(name: String, default: T): T =
        when (T::class) {
            String::class -> (System.getenv(name) ?: default) as T
            Boolean::class -> (System.getenv(name)?.toBoolean() ?: default) as T
            Int::class -> (System.getenv(name)?.toIntOrNull() ?: default) as T
            Long::class -> (System.getenv(name)?.toLongOrNull() ?: default) as T
            else -> default
        }