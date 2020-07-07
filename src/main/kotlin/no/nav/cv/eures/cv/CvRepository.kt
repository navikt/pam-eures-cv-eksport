package no.nav.cv.eures.cv

import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import javax.inject.Singleton
import javax.persistence.*


interface CvRepository {

    fun lagreCv(cv: CV)

    fun hentCv(aktorId: String) : CV?
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
    override fun hentCv(aktorId: String): CV? {
        return entityManager.createQuery(hentCv, CV::class.java)
                .setParameter("aktorId", aktorId)
                .resultList
                .firstOrNull()
    }

    @Transactional
    override fun lagreCv(cv: CV) {
        log.info("Lagrer cv for ${cv.aktorId}")
        entityManager.persist(cv)
    }
}



@Entity
@Table(name = "CV")
data class CV(
        @Id
        @Column(name = "ID")
        @GeneratedValue(generator = "CV_SEQ")
        private val id: Long = 0,

        @Column(name = "AKTOR_ID", nullable = false)
        val aktorId: String,

        @Column(name = "SIST_ENDRET", nullable = false)
        val sistEndret: ZonedDateTime,

        @Column(name = "RAW_AVRO", nullable = false)
        val rawAvro: String
)