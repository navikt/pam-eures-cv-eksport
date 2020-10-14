package no.nav.cv.eures.cv

import io.micronaut.spring.tx.annotation.Transactional
import no.nav.arbeid.cv.avro.Meldingstype
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Singleton
import javax.persistence.*


interface CvRepository {

    fun lagreCv(rawCv: RawCV)

    fun hentCvByAktoerId(aktoerId: String) : RawCV?

    fun hentCvByFoedselsnummer(foedselsnummer: String) : RawCV?

    fun hentUprosesserteCver(): List<RawCV>
}

// TODO - No FNR logging
@Singleton
private open class JpaCvRepository(
        @PersistenceContext private val entityManager: EntityManager
) : CvRepository {
    private val serieMedWhitespace = Regex("(\\s+)")

    companion object {
        val log = LoggerFactory.getLogger(JpaCvRepository::class.java)
    }

    private val hentCvByAktoerId =
            """
                SELECT * FROM CV_RAW
                WHERE AKTOER_ID = :aktoerId
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun hentCvByAktoerId(aktoerId: String): RawCV? {
        return entityManager.createNativeQuery(hentCvByAktoerId, RawCV::class.java)
                .setParameter("aktoerId", aktoerId)
                .resultList
                .map { it as RawCV }
                .firstOrNull()
    }


    private val hentCvByFoedselsnummer =
            """
                SELECT * FROM CV_RAW
                WHERE FOEDSELSNUMMER = :foedselsnummer
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun hentCvByFoedselsnummer(foedselsnummer: String): RawCV? {
        return entityManager.createNativeQuery(hentCvByFoedselsnummer, RawCV::class.java)
                .setParameter("foedselsnummer", foedselsnummer)
                .resultList
                .map { it as RawCV }
                .firstOrNull()
    }

    private val hentUprosesserteCver =
            """
                SELECT * FROM CV_RAW
                WHERE PROSESSERT = FALSE 
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun hentUprosesserteCver(): List<RawCV> =
            entityManager.createNativeQuery(hentUprosesserteCver, RawCV::class.java)
                    .resultList
                    .map { it as RawCV }

    @Transactional
    override fun lagreCv(rawCv: RawCV) {
        log.info("Lagrer cv for ${rawCv.foedselsnummer}")
        if(rawCv.rawAvro.length > 128_000)
            throw Exception("Raw avro string for aktoer ${rawCv.foedselsnummer} is larger than the limit of 128_000 bytes")

        entityManager.merge(rawCv)
    }
}



@Entity
@Table(name = "CV_RAW")
class RawCV {
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "CV_RAW_SEQ")
    private var id: Long? = null

    @Column(name = "AKTOER_ID", nullable = false, unique = true)
    lateinit var aktoerId: String

    @Column(name = "FOEDSELSNUMMER", nullable = false, unique = true)
    lateinit var foedselsnummer: String

    @Column(name = "SIST_ENDRET", nullable = false)
    lateinit var sistEndret: ZonedDateTime

    @Column(name = "RAW_AVRO", nullable = false)
    lateinit var rawAvro: String

    @Column(name = "PROSESSERT", nullable = false)
    var prosessert: Boolean = false

    @Column(name = "UNDER_OPPFOELGING", nullable = false)
    var underOppfoelging: Boolean = false

    @Column(name = "MELDINGSTYPE", nullable = false)
    lateinit var meldingstype: Meldingstype

    fun update(
            aktoerId: String? = null,
            foedselsnummer: String? = null,
            sistEndret: ZonedDateTime? = null,
            rawAvro: String? = null,
            underOppfoelging: Boolean? = null,
            meldingstype: Meldingstype
    ) : RawCV {
        this.aktoerId = aktoerId ?: this.aktoerId
        this.foedselsnummer = foedselsnummer ?: this.foedselsnummer
        this.sistEndret = sistEndret ?: this.sistEndret
        this.rawAvro = rawAvro ?: this.rawAvro
        this.underOppfoelging = underOppfoelging ?: this.underOppfoelging
        this.meldingstype = meldingstype

        this.prosessert = false

        return this
    }

    fun getWireBytes() : ByteArray
        = if (!rawAvro.isBlank()) Base64.getDecoder().decode(rawAvro) else ByteArray(0)

    override fun toString(): String {
        return "RawCV(aktoerId='$aktoerId', sistEndret=$sistEndret, rawAvro='$rawAvro')"
    }

    companion object {
        fun create(
                aktoerId: String,
                foedselsnummer: String,
                sistEndret: ZonedDateTime,
                rawAvro: String,
                underOppfoelging: Boolean? = false,
                meldingstype: Meldingstype
        ) = RawCV().update(aktoerId, foedselsnummer, sistEndret, rawAvro, underOppfoelging, meldingstype)
    }
}
