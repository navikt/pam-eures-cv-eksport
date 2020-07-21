package no.nav.cv

import java.time.ZonedDateTime
import java.util.*

data class CvTestData(
        val now: ZonedDateTime = ZonedDateTime.now(),
        val yesterday: ZonedDateTime = ZonedDateTime.now().minusDays(1),

        val aktorId1: String = "123",
        val aktorId2: String = "321",
        val aktorIdUkjent: String = "ukjent",

        val rawAvro1: String = Base64.getEncoder().encodeToString("raw avro string 1".toByteArray()),
        val rawAvro2: String = Base64.getEncoder().encodeToString("raw avro string 2".toByteArray()),

        val rawAvro1Base64: String = Base64.getEncoder().encodeToString(rawAvro1.toByteArray()),
        val rawAvro2Base64: String = Base64.getEncoder().encodeToString(rawAvro2.toByteArray())
)