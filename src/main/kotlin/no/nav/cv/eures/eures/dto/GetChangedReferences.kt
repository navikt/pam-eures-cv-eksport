package no.nav.cv.eures.eures.dto

import no.nav.cv.eures.cv.CvXml
import java.sql.Timestamp
import java.time.ZoneOffset

data class GetChangedReferences(
        val createdReferences: List<ChangedReference> = listOf(),
        val modifiedReferences: List<ChangedReference> = listOf(),
        val closedReferences: List<ChangedReference> = listOf()
) {
    data class ChangedReference(
            val creationTimestamp: Long,
            val lastModificationTimestamp: Long,
            val closingTimestamp: Long? = null,
            val reference: String,
            val status: String
    ) {
        val source: String = "NAV"

        constructor(cv: CvXml) : this(
                creationTimestamp = cv.opprettet.toInstant().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli(),
                lastModificationTimestamp = cv.sistEndret.toInstant().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli(),
                closingTimestamp = cv.slettet?.let { it.toInstant().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli() },
                reference = cv.reference,
                status = if (cv.slettet != null) "CLOSED" else "ACTIVE")
    }
}
