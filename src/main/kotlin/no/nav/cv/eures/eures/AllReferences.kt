package no.nav.cv.eures.eures

import no.nav.arbeid.cv.avro.Melding
import no.nav.cv.eures.cv.CvXml
import java.io.InvalidObjectException
import java.sql.Timestamp

data class AllReferences(
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
                reference = "${cv.id}",
                creationTimeStamp = Timestamp.from(cv.opprettet.toInstant()),
                lastModificationTimestamp = Timestamp.from(cv.sistEndret.toInstant())
        )
    }
}
