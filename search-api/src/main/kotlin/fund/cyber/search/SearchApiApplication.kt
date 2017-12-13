package fund.cyber.search

import fund.cyber.cassandra.model.ChainsIndex.INDEX_TO_CHAIN_ENTITY
import fund.cyber.search.configuration.AppContext
import fund.cyber.search.configuration.SearchApiConfiguration
import fund.cyber.search.handler.*
import io.undertow.Handlers
import io.undertow.Undertow


object SearchApiApplication {

    @JvmStatic
    fun main(args: Array<String>) {

        val bitcoinRepository = AppContext.cassandraService.bitcoinRepository
        val ethereumRepository = AppContext.cassandraService.ethereumRepository

        val httpHandler = Handlers.routing()
                .get("/index-stats", IndexStatusHandler(indexToChainEntity = INDEX_TO_CHAIN_ENTITY))
                .get("/search", SearchHandler(indexToChainEntity = INDEX_TO_CHAIN_ENTITY))
                .get("/ping", PingHandler())
                .get("/bitcoin/block/{blockNumber}", BitcoinBlockHandler(bitcoinRepository))
                .get("/bitcoin/tx/{txHash}", BitcoinTxHandler(bitcoinRepository))
                .get("/bitcoin/address/{address}", BitcoinAddressHandler(bitcoinRepository))
                .get("/ethereum/block/{blockNumber}", EthereumBlockHandler(ethereumRepository))
                .get("/ethereum/tx/{txHash}", EthereumTxHandler(ethereumRepository))
                .get("/ethereum/address/{address}", EthereumAddressHandler(ethereumRepository))


        val setCorsHeaderHandler = SetCorsHeadersHandler(httpHandler, SearchApiConfiguration.allowedCORS)

        Undertow.builder()
                .addHttpListener(10300, "0.0.0.0")
                .setHandler(setCorsHeaderHandler)
                .build().start()
    }
}