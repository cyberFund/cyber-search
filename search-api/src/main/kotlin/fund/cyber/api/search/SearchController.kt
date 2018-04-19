package fund.cyber.api.search

import org.elasticsearch.action.support.IndicesOptions
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class SearchController(
        private val elasticClient: TransportClient
) {

    //todo slice caching, general web caching
    @GetMapping("/search")
    fun search(
            @RequestParam query: String,
            @RequestParam(required = false, defaultValue = "0") page: Int,
            @RequestParam(required = false, defaultValue = "10") pageSize: Int,
            @RequestParam(required = false, defaultValue = "") chains: Array<String>,
            @RequestParam(required = false, defaultValue = "") types: Array<String>
    ): Mono<SearchResponse> {

        val elasticQuery = QueryBuilders.matchQuery("_all", query)
                .fuzziness(Fuzziness.ZERO)

        val elasticResponse = elasticClient.prepareSearch(*prepareIndices(chains, types))
                .setIndicesOptions(IndicesOptions.lenientExpandOpen())
                .setTypes()
                .setQuery(elasticQuery)
                .setFrom(page * pageSize).setSize(pageSize).setExplain(true)
                .get()

        val responseItems = elasticResponse.hits.map { hit ->
            val chain = hit.index.substringBefore(".")
            val entity = hit.index.substringAfter(".")
            ItemPreview(chain, entity, hit.sourceAsString)
        }

        return Mono.just(
                SearchResponse(
                        query = query, page = page, pageSize = pageSize, totalHits = elasticResponse.hits.totalHits,
                        items = responseItems, searchTime = elasticResponse.tookInMillis
                )
        )
    }

    private fun prepareIndices(chains: Array<String>, types: Array<String>): Array<String> {
        val chainsToFilter = if (chains.isEmpty()) arrayOf("*") else chains
        val typesToFilter = if (types.isEmpty()) arrayOf("*") else types

        val indicesToFilter = chainsToFilter.flatMap { chain -> typesToFilter.map { type -> "$chain.$type" } }
        return indicesToFilter.toTypedArray()
    }
}
