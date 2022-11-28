package no.nav.cv.eures.cv

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.*

interface CvRepository : JpaRepository<RawCV, Long> {

    @Query("SELECT cv FROM RawCV cv WHERE cv.aktoerId = ?1")
    fun hentCvByAktoerId(aktoerId: String) : RawCV?

    @Query("SELECT cv FROM RawCV cv WHERE cv.foedselsnummer = ?1")
    fun hentCvByFoedselsnummer(foedselsnummer: String) : RawCV?

    @Query("SELECT cv FROM RawCV cv WHERE cv.prosessert = false")
    fun hentUprosesserteCver(): List<RawCV>

    @Query("SELECT cv FROM RawCV cv JOIN CvXml cvXml ON cv.foedselsnummer = cvXml.foedselsnummer " +
            "WHERE cv.sistEndret < ?1 AND cvXml.slettet IS NULL")
    fun hentGamleCver(time: ZonedDateTime): List<RawCV>

    @Query("SELECT COUNT(*) FROM CV_RAW", nativeQuery = true)
    fun fetchCountRawCvs() : Long

    @Transactional
    @Modifying
    @Query("DELETE FROM CV_RAW raw WHERE raw.AKTOER_ID = ?1", nativeQuery = true)
    fun deleteCvByAktorId(aktoerId: String)
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

    @Column(name = "RAW_AVRO", nullable = false )
    var rawAvro: String? = ""

    @Column(name = "PROSESSERT", nullable = false)
    var prosessert: Boolean = false

    @Column(name = "UNDER_OPPFOELGING", nullable = false)
    var underOppfoelging: Boolean = false

    @Column(name = "MELDINGSTYPE", nullable = false)
    lateinit var meldingstype: RecordType

    @Column(name = "JSON_CV", nullable = true)
    var jsonCv: String? = null

    fun update(
            aktoerId: String? = null,
            foedselsnummer: String? = null,
            sistEndret: ZonedDateTime? = null,
            rawAvro: String? = "",
            underOppfoelging: Boolean? = null,
            meldingstype: RecordType,
            jsonCv: String? = null
    ) : RawCV {
        this.aktoerId = aktoerId ?: this.aktoerId
        this.foedselsnummer = foedselsnummer ?: this.foedselsnummer
        this.sistEndret = sistEndret ?: this.sistEndret
        this.rawAvro = rawAvro ?: this.rawAvro
        this.underOppfoelging = underOppfoelging ?: this.underOppfoelging
        this.meldingstype = meldingstype
        this.jsonCv = jsonCv ?: this.jsonCv
        this.prosessert = false

        return this
    }

    fun getWireBytes() : ByteArray
        = if (!rawAvro?.isBlank()!!) Base64.getDecoder().decode(rawAvro) else ByteArray(0)

    override fun toString(): String {
        return "RawCV(aktoerId='$aktoerId', sistEndret=$sistEndret, rawAvro='$rawAvro')"
    }

    companion object {
        enum class RecordType {
            CREATE, UPDATE, DELETE
        }

        fun create(
                aktoerId: String,
                foedselsnummer: String,
                sistEndret: ZonedDateTime,
                rawAvro: String? = "",
                underOppfoelging: Boolean? = false,
                meldingstype: RecordType,
                jsonCv: String? = null
        ) = RawCV().update(aktoerId, foedselsnummer, sistEndret, rawAvro, underOppfoelging, meldingstype, jsonCv)
    }
}
