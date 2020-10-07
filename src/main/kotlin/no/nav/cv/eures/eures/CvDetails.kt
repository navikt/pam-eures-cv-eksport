package no.nav.cv.eures.eures

import com.fasterxml.jackson.annotation.JsonInclude
import java.sql.Timestamp

data class CvDetails(
        val details: Map<String, CandidateDetail> = mapOf()
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class CandidateDetail(
            val creationTimestamp: Timestamp? = null,
            val lastModificationTimestamp: Timestamp? = null,
            val closingTimestamp: Timestamp? = null,
            val reference: String,
            val status: String,
            val content: String? = null
    ) {
        val source: String = "NAV"
        val contentFormatVersion: String = "1.0"
    }
}
