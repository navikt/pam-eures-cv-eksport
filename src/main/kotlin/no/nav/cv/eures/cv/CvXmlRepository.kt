package no.nav.cv.eures.cv

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime
import javax.persistence.*

interface CvXmlRepository : JpaRepository<CvXml, Long> {

    @Query("SELECT cv FROM CvXml cv WHERE cv.foedselsnummer = ?1")
    fun fetch(foedselsnummer: String?): CvXml?

    @Query("SELECT cv FROM CvXml cv WHERE cv.aktorId = ?1")
    fun fetchByAktorId(aktorId: String?): CvXml?

    @Query("SELECT COUNT(cv) FROM CvXml cv WHERE cv.slettet IS NULL AND cv.opprettet = cv.sistEndret")
    fun fetchCountCreated(): Long

    @Query("SELECT COUNT(cv) FROM CvXml cv WHERE cv.slettet IS NULL AND cv.opprettet <> cv.sistEndret")
    fun fetchCountModified(): Long

    @Query("SELECT COUNT(cv) FROM CvXml cv WHERE cv.slettet IS NOT NULL")
    fun fetchCountClosed(): Long

    @Query(value = """
        SELECT * FROM CV_XML
        WHERE SLETTET IS NULL
        AND EXISTS(
            SELECT * FROM SAMTYKKE 
            WHERE SAMTYKKE.FOEDSELSNUMMER = CV_XML.FOEDSELSNUMMER
        )
        """,
        countQuery = """
        SELECT count(*) FROM CV_XML
        WHERE SLETTET IS NULL
        AND EXISTS(
            SELECT * FROM SAMTYKKE 
            WHERE SAMTYKKE.FOEDSELSNUMMER = CV_XML.FOEDSELSNUMMER
        )
        """,
        nativeQuery = true)
    fun fetchAllActive(page: Pageable): Page<CvXml>

    @Query("""
    SELECT * FROM CV_XML
                WHERE SLETTET IS NULL
                AND FOEDSELSNUMMER IN :foedselsnummer
                AND EXISTS(
                    SELECT * FROM SAMTYKKE 
                    WHERE SAMTYKKE.FOEDSELSNUMMER = CV_XML.FOEDSELSNUMMER
                )
                """, nativeQuery = true)
    fun fetchAllActiveCvsByFoedselsnummer(foedselsnummer: List<String>): List<CvXml>

    @Query("SELECT cv FROM CvXml cv WHERE cv.reference in :references")
    fun fetchAllCvsByReference(references: List<String>): List<CvXml>

    @Query("SELECT cv FROM CvXml cv WHERE cv.sistEndret > :time or cv.slettet > :time")
    fun fetchAllCvsAfterTimestamp(page: Pageable, time: ZonedDateTime): Page<CvXml>

}


@Entity
@Table(name = "CV_XML")
class CvXml {
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "CV_XML_SEQ")
    var id: Long? = null

    @Column(name = "FOEDSELSNUMMER", nullable = false, unique = true)
    lateinit var foedselsnummer: String

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

    @Column(name = "CHECKSUM", nullable = false)
    lateinit var checksum: String

    @Column(name = "AKTORID", nullable = true)
    lateinit var aktorId: String

    fun update(
            reference: String,
            foedselsnummer: String,
            opprettet: ZonedDateTime,
            sistEndret: ZonedDateTime,
            slettet: ZonedDateTime?,
            xml: String,
            checksum: String
    ): CvXml {
        this.reference = reference
        this.foedselsnummer = foedselsnummer
        this.opprettet = opprettet
        this.sistEndret = sistEndret
        this.slettet = slettet
        this.xml = xml
        this.checksum = checksum

        return this
    }

    override fun toString(): String {
        return "CvXml(aktoerId='$foedselsnummer', opprettet=$opprettet, sistEndret=$sistEndret, slettet=$slettet, checksum='$checksum', xml='$xml')"
    }

    companion object {
        fun create(
                reference: String,
                foedselsnummer: String,
                opprettet: ZonedDateTime,
                sistEndret: ZonedDateTime,
                slettet: ZonedDateTime?,
                xml: String,
                checksum: String
        ) = CvXml().update(reference, foedselsnummer, opprettet, sistEndret, slettet, xml, checksum)
    }
}
