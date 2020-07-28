package no.nav.cv.eures.cv

import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Singleton
import javax.persistence.*


interface CvRepository {

    fun lagreCv(rawCv: RawCV)

    fun hentCv(aktoerId: String) : RawCV?
}

@Singleton
private open class JpaCvRepository(
        @PersistenceContext private val entityManager: EntityManager
) : CvRepository {
    private val serieMedWhitespace = Regex("(\\s+)")

    companion object {
        val log = LoggerFactory.getLogger(JpaCvRepository::class.java)
    }

    private val hentCv =
            """
                SELECT * FROM CV_RAW
                WHERE AKTOER_ID = :aktoerId
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun hentCv(aktoerId: String): RawCV? {
        return entityManager.createNativeQuery(hentCv, RawCV::class.java)
                .setParameter("aktoerId", aktoerId)
                .resultList
                .map { it as RawCV }
                .firstOrNull()
    }

    @Transactional
    override fun lagreCv(rawCv: RawCV) {
        log.info("Lagrer cv for ${rawCv.aktoerId}")
        if(rawCv.rawAvro.length > 128_000)
            throw Exception("Raw avro string for aktoer ${rawCv.aktoerId} is larger than the limit of 128_000 bytes")

        entityManager.merge(rawCv)
    }
}



@Entity
@Table(name = "CV_RAW")
class RawCV() {
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "CV_SEQ")
    private var id: Long? = null

    @Column(name = "AKTOER_ID", nullable = false, unique = true)
    lateinit var aktoerId: String

    @Column(name = "SIST_ENDRET", nullable = false)
    lateinit var sistEndret: ZonedDateTime

    @Column(name = "RAW_AVRO", nullable = false)
    lateinit var rawAvro: String

    fun update(aktoerId: String, sistEndret: ZonedDateTime, rawAvro: String) : RawCV {
        this.aktoerId = aktoerId
        this.sistEndret = sistEndret
        this.rawAvro = rawAvro

        return this
    }

    fun getAvroBytes() : ByteArray
        = Base64.getDecoder().decode(this.rawAvro)

    override fun toString(): String {
        return "RawCV(aktoerId='$aktoerId', sistEndret=$sistEndret, rawAvro='$rawAvro')"
    }

    companion object {
        fun create(aktoerId: String, sistEndret: ZonedDateTime, rawAvro: String)
                = RawCV().update(aktoerId, sistEndret, rawAvro)
    }
}