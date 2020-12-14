package no.nav.cv.eures.eures.dto

import no.nav.cv.eures.cv.CvXml
import java.sql.Timestamp
import java.time.ZoneOffset

data class GetAllReferences(
        val allReferences: List<Reference> = listOf()
) {

    data class Reference(
            val reference: String,
            val creationTimeStamp: Long,
            val lastModificationTimestamp: Long
    ) {
        val source: String = "NAV"
        val status: String = "ACTIVE"

        constructor(cv: CvXml) : this(
                reference = cv.reference,
                creationTimeStamp = cv.opprettet.toInstant().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli(),
                lastModificationTimestamp = cv.sistEndret.toInstant().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
    }
}
