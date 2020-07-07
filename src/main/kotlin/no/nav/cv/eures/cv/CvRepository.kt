package no.nav.cv.eures.cv

import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import javax.inject.Singleton
import javax.persistence.*


interface CvRepository {

    fun lagreCv(rawCv: RawCV)

    fun hentCv(aktorId: String) : RawCV?
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
                SELECT * FROM CV
                WHERE AKTOR_ID = :aktorId
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun hentCv(aktorId: String): RawCV? {
        return entityManager.createNativeQuery(hentCv, RawCV::class.java)
                .setParameter("aktorId", aktorId)
                .resultList
                .map { it as RawCV }
                .firstOrNull()
    }

    @Transactional
    override fun lagreCv(rawCv: RawCV) {
        log.info("Lagrer cv for ${rawCv.aktorId}")
        entityManager.merge(rawCv)
    }
}



@Entity
@Table(name = "CV")
class RawCV() {
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "CV_SEQ")
    private var id: Long? = null

    @Column(name = "AKTOR_ID", nullable = false, unique = true)
    lateinit var aktorId: String

    @Column(name = "SIST_ENDRET", nullable = false)
    lateinit var sistEndret: ZonedDateTime

    @Column(name = "RAW_AVRO", nullable = false)
    lateinit var rawAvro: String

    fun update(aktorId: String, sistEndret: ZonedDateTime, rawAvro: String) : RawCV {
        this.aktorId = aktorId
        this.sistEndret = sistEndret
        this.rawAvro = rawAvro

        return this
    }

    companion object {
        fun create(aktorId: String, sistEndret: ZonedDateTime, rawAvro: String)
                = RawCV().update(aktorId, sistEndret, rawAvro)
    }
}