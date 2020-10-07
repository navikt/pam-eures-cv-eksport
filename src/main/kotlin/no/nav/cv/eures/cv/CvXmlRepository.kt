package no.nav.cv.eures.cv

import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import javax.inject.Singleton
import javax.persistence.*

interface CvXmlRepository {

    fun hentCv(aktoerId: String) : CvXml?

    fun hentAlle(): List<CvXml>

    fun hentAlle(ids: List<Long>) : List<CvXml>

    fun hentAlleEtter(timestamp: ZonedDateTime): List<CvXml>

    fun lagreCvXml(cvXml: CvXml)

}


@Singleton
private open class JpaCvXMLRepository(
        @PersistenceContext private val entityManager: EntityManager
) : CvXmlRepository {
    private val serieMedWhitespace = Regex("(\\s+)")

    companion object {
        val log = LoggerFactory.getLogger(JpaCvXMLRepository::class.java)
    }

    private val hentCv =
            """
                SELECT * FROM CV_XML
                WHERE AKTOER_ID > :aktoerId
            """.replace(serieMedWhitespace, " ")

    private val hentAlleAktiveCver =
            """
                SELECT * FROM CV_XML
                WHERE SLETTET IS NULL
            """.replace(serieMedWhitespace, " ")

    private val hentAlleAktiveCverMedIder =
            """
                SELECT * FROM CV_XML
                WHERE SLETTET IS NULL
                AND CV_XML.ID IN :ids
            """.replace(serieMedWhitespace, " ")

    private val hentAlleCverEtter =
            """
                SELECT * FROM CV_XML
                WHERE SIST_ENDRET > :timestamp
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun hentCv(aktoerId: String): CvXml? {
        return entityManager.createNativeQuery(hentCv, CvXml::class.java)
                .setParameter("aktoerId", aktoerId)
                .resultList
                .map { it as CvXml }
                .firstOrNull()
    }

    @Transactional(readOnly = true)
    override fun hentAlle(ids: List<Long>): List<CvXml> {
        return entityManager.createNativeQuery(hentAlleAktiveCverMedIder, CvXml::class.java)
                .setParameter("ids", ids)
                .resultList
                .map { it as CvXml }
    }

    @Transactional(readOnly = true)
    override fun hentAlle(): List<CvXml> {
        return entityManager.createNativeQuery(hentAlleAktiveCver, CvXml::class.java)
                .resultList
                .map { it as CvXml }
    }

    @Transactional(readOnly = true)
    override fun hentAlleEtter(time: ZonedDateTime): List<CvXml> {
        return entityManager.createNativeQuery(hentAlleCverEtter, CvXml::class.java)
                .setParameter("timestamp", time)
                .resultList
                .map { it as CvXml }
    }

    @Transactional
    override fun lagreCvXml(cvXml: CvXml) {
        log.info("Lagrer cv for ${cvXml.aktoerId}")

        if(cvXml.xml.length > 128_000)
            throw Exception("Cv XML string for aktoer ${cvXml.aktoerId} is larger than the limit of 128_000 bytes")

        entityManager.merge(cvXml)
    }

}

@Entity
@Table(name = "CV_XML")
class CvXml() {
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "CV_SEQ")
    var id: Long? = null

    @Column(name = "AKTOER_ID", nullable = false, unique = true)
    lateinit var aktoerId: String

    @Column(name = "OPPRETTET", nullable = false)
    lateinit var opprettet: ZonedDateTime

    @Column(name = "SIST_ENDRET", nullable = false)
    lateinit var sistEndret: ZonedDateTime

    @Column(name = "SLETTET", nullable = true)
    var slettet: ZonedDateTime? = null

    @Column(name = "XML", nullable = false)
    lateinit var xml: String

    fun update(
            aktoerId: String,
            opprettet: ZonedDateTime,
            sistEndret: ZonedDateTime,
            slettet: ZonedDateTime?,
            xml: String
    ) : CvXml {
        this.aktoerId = aktoerId
        this.opprettet = opprettet
        this.sistEndret = sistEndret
        this.slettet = slettet
        this.xml = xml

        return this
    }

    override fun toString(): String {
        return "CvXml(aktoerId='$aktoerId', opprettet=$opprettet, sistEndret=$sistEndret, slettet=$slettet, xml='$xml')"
    }

    companion object {
        fun create(
                aktoerId: String,
                opprettet: ZonedDateTime,
                sistEndret: ZonedDateTime,
                slettet: ZonedDateTime?,
                xml: String
        ) = CvXml().update(aktoerId, opprettet, sistEndret, slettet, xml)
    }
}
