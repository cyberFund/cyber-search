package fund.cyber.node.model

import com.fasterxml.jackson.annotation.JsonRawValue


data class ItemPreview(
        val type: String,
        @JsonRawValue val data: String
)

data class DocumentKey(
        val catalog: String,
        val type: String,
        val id: String
)

data class SearchRequestProcessingStats(
        val raw_request: String,
        val time: String,
        val time_m: Long,
        val max_score: Float,
        val total_hits: Long,
        val search_time_ms: Long,
        val documents: List<DocumentKey>
)