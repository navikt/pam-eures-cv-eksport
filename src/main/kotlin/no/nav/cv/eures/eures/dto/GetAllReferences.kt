package no.nav.cv.eures.eures.dto

import no.nav.cv.eures.cv.CvXml
import java.sql.Timestamp

data class GetAllReferences(
        val allReferences: List<Reference> = listOf()
) {

    data class Reference(
            val reference: String,
            val creationTimeStamp: Timestamp,
            val lastModificationTimestamp: Timestamp
    ) {
        val source: String = "NAV"
        val status: String = "ACTIVE"

        constructor(cv: CvXml) : this(
                reference = cv.reference,
                creationTimeStamp = Timestamp.from(cv.opprettet.toInstant()),
                lastModificationTimestamp = Timestamp.from(cv.sistEndret.toInstant())
        )
    }
}
