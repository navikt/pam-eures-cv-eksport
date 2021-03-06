package no.nav.cv.eures.cv

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime
import javax.persistence.*

interface CvXmlRepository : JpaRepository<CvXml, Long> {

    @Query("SELECT cv FROM CvXml cv WHERE cv.foedselsnummer = ?1")
    fun fetch(foedselsnummer: String): CvXml?

    @Query("SELECT cv FROM CvXml cv")
    fun fetchAll(): List<CvXml>

    @Query("""
    SELECT * FROM CV_XML
                WHERE SLETTET IS NULL
                AND EXISTS(
                    SELECT * FROM SAMTYKKE 
                    WHERE SAMTYKKE.FOEDSELSNUMMER = CV_XML.FOEDSELSNUMMER
                )
    """, nativeQuery = true)
    fun fetchAllActive(): List<CvXml>

    @Query("""
    SELECT * FROM CV_XML
                WHERE SLETTET IS NULL
                AND FOEDSELSNUMMER IN (?1)
                AND EXISTS(
                    SELECT * FROM SAMTYKKE 
                    WHERE SAMTYKKE.FOEDSELSNUMMER = CV_XML.FOEDSELSNUMMER
                )
                """, nativeQuery = true)
    fun fetchAllActiveCvsByFoedselsnummer(foedselsnummer: List<String>): List<CvXml>

    @Query("SELECT cv FROM CvXml cv WHERE cv.reference in ?1")
    fun fetchAllCvsByReference(references: List<String>): List<CvXml>

    @Query("SELECT cv FROM CvXml cv WHERE cv.sistEndret > ?1 or cv.slettet > ?1")
    fun fetchAllCvsAfterTimestamp(time: ZonedDateTime): List<CvXml>

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
