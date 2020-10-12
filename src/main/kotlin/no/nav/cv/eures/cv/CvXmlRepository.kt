package no.nav.cv.eures.cv

import io.micronaut.spring.tx.annotation.Transactional
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import javax.inject.Singleton
import javax.persistence.*

interface CvXmlRepository {

    fun fetch(aktoerId: String) : CvXml?

    fun fetchAllActive(): List<CvXml>

    fun fetchAllActiveCvsByAktoerId(aktoerIder: List<String>) : List<CvXml>

    fun fetchAllCvsByReference(references: List<String>) : List<CvXml>

    fun fetchAllCvsAfterTimestamp(time: ZonedDateTime): List<CvXml>

    fun save(cvXml: CvXml) : CvXml

}

@Singleton
private open class JpaCvXMLRepository(
        @PersistenceContext private val entityManager: EntityManager
) : CvXmlRepository {
    private val serieMedWhitespace = Regex("(\\s+)")

    companion object {
        val log: Logger = LoggerFactory.getLogger(JpaCvXMLRepository::class.java)
    }

    private val fetchQuery =
            """
                SELECT * FROM CV_XML
                WHERE AKTOER_ID = :aktoerId
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun fetch(aktoerId: String): CvXml? =
            try {
                entityManager.createNativeQuery(fetchQuery, CvXml::class.java)
                    .setParameter("aktoerId", aktoerId)
                    .resultList
                    .map { it as CvXml }
                    .firstOrNull()
            } catch (e: Exception) {
                log.error("Feil ved henting av XML data for aktoer: $aktoerId", e)
                null
            }

    private val fetchAllActiveQuery =
            """
                SELECT * FROM CV_XML
                WHERE SLETTET IS NULL
                AND EXISTS(
                    SELECT * FROM SAMTYKKE 
                    WHERE SAMTYKKE.AKTOER_ID = CV_XML.AKTOER_ID
                )
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun fetchAllActive(): List<CvXml> =
            try {
                entityManager.createNativeQuery(fetchAllActiveQuery, CvXml::class.java)
                    .resultList
                    .map { it as CvXml }
            } catch (e: Exception) {
                log.error("Feil ved hending av aktive XML dataer", e)
                listOf()
            }

    private val fetchAllActiveCvsByAktoerIdQuery =
            """
                SELECT * FROM CV_XML
                WHERE SLETTET IS NULL
                AND AKTOER_ID IN (:aktoerIder)
                AND EXISTS(
                    SELECT * FROM SAMTYKKE 
                    WHERE SAMTYKKE.AKTOER_ID = CV_XML.AKTOER_ID
                )
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun fetchAllActiveCvsByAktoerId(aktoerIder: List<String>): List<CvXml> = try {
        entityManager.createNativeQuery(fetchAllActiveCvsByAktoerIdQuery, CvXml::class.java)
                .setParameter("aktoerIder", aktoerIder)
                .resultList
                .map { it as CvXml }
        } catch (e: Exception) {
        log.error("Feil ved henting av XML dataer for aktoer ider: $aktoerIder", e)
        listOf()
    }

    @Language("POSTGRES-PSQL")
    private val fetchAllCvsAfterTimestampQuery =
            """
                SELECT * FROM CV_XML
                WHERE SIST_ENDRET > :timestamp 
                OR SLETTET > :timestamp
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun fetchAllCvsAfterTimestamp(time: ZonedDateTime): List<CvXml> = try {
        entityManager.createNativeQuery(fetchAllCvsAfterTimestampQuery, CvXml::class.java)
                .setParameter("timestamp", time)
                .resultList
                .map { it as CvXml }
    } catch (e: Exception) {
        log.error("Feil ved henting av XML data etter timestamp: $time", e)
        listOf()
    }

    private val fetchAllCvsByReferenceQuery =
            """
                SELECT * FROM CV_XML
                WHERE REFERANSE IN (:references)
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun fetchAllCvsByReference(references: List<String>): List<CvXml>  = try {
        entityManager.createNativeQuery(fetchAllCvsByReferenceQuery, CvXml::class.java)
                .setParameter("references", references)
                .resultList
                .map { it as CvXml }
    } catch (e: Exception) {
        log.error("Feil ved henting av XML data for referanser: $references", e)
        listOf()
    }

    @Transactional
    override fun save(cvXml: CvXml) : CvXml {
        log.info("Lagrer cv for ${cvXml.aktoerId}")

        if(cvXml.xml.length > 128_000)
            throw Exception("Cv XML string for aktoer ${cvXml.aktoerId} is larger than the limit of 128_000 bytes")

        return try {
            entityManager.merge(cvXml)
        } catch (e: Exception) {
            log.error("Feil ved lagring av XML data for aktoer: ${cvXml.aktoerId}", e)
            throw Exception("Feil ved lagring av XML data for aktoer: ${cvXml.aktoerId}", e)
        }
    }

}

@Entity
@Table(name = "CV_XML")
class CvXml {
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "CV_SEQ")
    var id: Long? = null

    @Column(name = "AKTOER_ID", nullable = false, unique = true)
    lateinit var aktoerId: String

    @Column(name = "REFERANSE", nullable = false, unique = true)
    lateinit var reference: String

    @Column(name = "OPPRETTET", nullable = false)
    lateinit var opprettet: ZonedDateTime

    @Column(name = "SIST_ENDRET", nullable = false)
    lateinit var sistEndret: ZonedDateTime

    @Column(name = "SLETTET", nullable = true)
    var slettet: ZonedDateTime? = null

    @Column(name = "XML", nullable = false)
    lateinit var xml: String

    fun update(
            reference: String,
            aktoerId: String,
            opprettet: ZonedDateTime,
            sistEndret: ZonedDateTime,
            slettet: ZonedDateTime?,
            xml: String
    ) : CvXml {
        this.reference = reference
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
                reference: String,
                aktoerId: String,
                opprettet: ZonedDateTime,
                sistEndret: ZonedDateTime,
                slettet: ZonedDateTime?,
                xml: String
        ) = CvXml().update(reference, aktoerId, opprettet, sistEndret, slettet, xml)
    }
}
