package no.nav.cv.eures.eures.dto

import no.nav.cv.eures.cv.CvXml
import java.sql.Timestamp

data class GetChangedReferences(
        val createdReferences: List<ChangedReference> = listOf(),
        val modifiedReferences: List<ChangedReference> = listOf(),
        val closedReferences: List<ChangedReference> = listOf()
) {
    data class ChangedReference(
            val creationTimestamp: Timestamp,
            val lastModificationTimestamp: Timestamp,
            val closingTimestamp: Timestamp? = null,
            val reference: String,
            val status: String
    ) {
        val source: String = "NAV"

        constructor(cv: CvXml) : this(
                creationTimestamp = Timestamp.from(cv.opprettet.toInstant()),
                lastModificationTimestamp = Timestamp.from(cv.sistEndret.toInstant()),
                closingTimestamp = cv.slettet?.let { Timestamp.from(it.toInstant()) },
                reference = cv.reference,
                status = if (cv.slettet != null) "CLOSED" else "ACTIVE")
    }
}
