package no.nav.cv.eures.eures.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.sql.Timestamp

data class GetDetails(
        val details: Map<String, CandidateDetail> = mapOf()
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class CandidateDetail(
            val creationTimestamp: Long? = null,
            val lastModificationTimestamp: Long? = null,
            val closingTimestamp: Long? = null,
            val reference: String,
            val status: Status,
            val content: String? = null
    ) {
        val source: String = "NAV"
        val contentFormatVersion: String = "1.0"

        enum class Status {
            CLOSED, ACTIVE
        }
    }
}
