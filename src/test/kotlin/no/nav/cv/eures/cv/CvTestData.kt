package no.nav.cv.eures.cv

import no.nav.arbeid.cv.avro.Cv
import no.nav.arbeid.cv.avro.Melding
import no.nav.arbeid.cv.avro.Meldingstype
import no.nav.arbeid.cv.avro.OpprettCv
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.commons.io.output.ByteArrayOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

private fun createDummyCv(aktoerId: String, fnr: String, created: Instant) = Cv.newBuilder()
        .setCvId(aktoerId)
        .setFornavn("Testen")
        .setEtternavn("Testenson")
        .setAktoerId(aktoerId)
        .setFodselsnummer(fnr)
        .setFoedselsdato(LocalDate.of(1990, 1, 1))
        .setFoererkort(null)
        .setDisponererBil(false)
        .setUtdannelse(listOf())
        .setArbeidserfaring(listOf())
        .setAnnenErfaring(listOf())
        .setKurs(listOf())
        .setFagdokumentasjon(listOf())
        .setSpraakferdigheter(listOf())
        .setSertifikat(listOf())
        .setArenaKandidatnr(aktoerId)
        .setOpprettet(created)
        .setSistEndret(created)
        .build()

fun Melding.toByteArray(): ByteArray = ByteArrayOutputStream().let { out ->
    EncoderFactory.get().binaryEncoder(out, null).let { encoder ->
        SpecificDatumWriter<Melding>(Melding.getClassSchema()).write(this, encoder)
        encoder.flush()
        out.close()
        byteArrayOf(0,0,0,0,0) + out.toByteArray()
    }
}

data class CvTestData(
        val now: ZonedDateTime = ZonedDateTime.now(),
        val yesterday: ZonedDateTime = ZonedDateTime.now().minusDays(1),

        val aktoerId1: String = "123",
        val aktoerId2: String = "321",

        val foedselsnummer1: String = "12345",
        val foedselsnummer2: String = "54321",

        val foedselsdato1: LocalDate = LocalDate.of(1980, 1, 1),
        val foedselsdato2: LocalDate = LocalDate.of(1990, 1, 1),

        val cv1: Cv = createDummyCv(aktoerId1, foedselsnummer1, now.toInstant()),
        val cv2: Cv = createDummyCv(aktoerId2, foedselsnummer2, yesterday.toInstant()),

        val foedselsnummerUkjent: String = "ukjent",

        val underOppfoelging: Boolean = false,

        val melding1: Melding = Melding.newBuilder()
                .setAktoerId(aktoerId1)
                .setOpprettCv(OpprettCv(cv1))
                .setMeldingstype(Meldingstype.OPPRETT)
                .setSistEndret(now.toInstant())
                .build(),

        val melding2: Melding = Melding.newBuilder()
                .setAktoerId(aktoerId2)
                .setOpprettCv(OpprettCv(cv2))
                .setMeldingstype(Meldingstype.OPPRETT)
                .setSistEndret(yesterday.toInstant())
                .build(),

        val rawAvro1Base64: String = Base64.getEncoder().encodeToString(melding1.toByteArray()),
        val rawAvro2Base64: String = Base64.getEncoder().encodeToString(melding2.toByteArray())
)
