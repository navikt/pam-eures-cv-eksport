package no.nav.cv.eures.eures

data class CvDetails(
        val details: Map<String, CandidateDetail> = mapOf()
) {
    data class CandidateDetail(
            val creationTimestamp: Long = 0,
            val lastModificationTimestamp: Long = 0,
            val reference: String = "",
            val status: String = "",
            val content: String = ""
    ) {
        val source: String = "NAV"
        val contentFormatVersion: String = "1.0"
    }
}
