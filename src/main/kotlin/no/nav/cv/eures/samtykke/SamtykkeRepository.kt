package no.nav.cv.eures.samtykke

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigInteger
import java.time.ZonedDateTime
import jakarta.persistence.*

interface SamtykkeRepository {

    fun hentSamtykke(foedselsnummer: String) : Samtykke?
    fun hentSamtykkeUtenNaaverendeXml(foedselsnummer: List<String>) : List<SamtykkeEntity>
    fun hentGamleSamtykker(time: ZonedDateTime): List<SamtykkeEntity>
    fun hentAntallSamtykker() : Long
    fun hentAntallSamtykkerPerKategori(): Map<String, Long>
    fun slettSamtykke(foedselsnummer: String) : Int
    fun oppdaterSamtykke(foedselsnummer: String, samtykke: Samtykke)
    fun finnFoedselsnumre() : List<String>
    fun hentAlleLand() : List<String>
}

@Repository
private open class JpaSamtykkeRepository(
        @PersistenceContext private val entityManager: EntityManager
) : SamtykkeRepository {
    private val serieMedWhitespace = Regex("(\\s+)")

    companion object {
        val log: Logger = LoggerFactory.getLogger(SamtykkeRepository::class.java)
    }

    private val hentSamtykke =
            """
                SELECT * FROM SAMTYKKE
                WHERE FOEDSELSNUMMER = :foedselsnummer
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun hentSamtykke(foedselsnummer: String)
            = entityManager.createNativeQuery(hentSamtykke, SamtykkeEntity::class.java)
                .setParameter("foedselsnummer", foedselsnummer)
                .resultList
                .map { it as SamtykkeEntity }
                .map { it.toSamtykke() }
                .firstOrNull()

    private val hentSamtykkeUtenNaavaerendeXmlQuery =
            """
                SELECT * FROM SAMTYKKE
                WHERE FOEDSELSNUMMER IN (:foedselsnummer)
                AND NOT EXISTS(
                    SELECT * FROM CV_XML 
                    WHERE SAMTYKKE.FOEDSELSNUMMER = CV_XML.FOEDSELSNUMMER
                )
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun hentSamtykkeUtenNaaverendeXml(foedselsnummer: List<String>)
            = entityManager.createNativeQuery(hentSamtykkeUtenNaavaerendeXmlQuery, SamtykkeEntity::class.java)
                .setParameter("foedselsnummer", foedselsnummer)
                .resultList
                .map { it as SamtykkeEntity }

    private val hentGamleSamtykkerQuery =
        """
            SELECT * FROM SAMTYKKE
            WHERE SIST_ENDRET < :olderThan
        """.replace(serieMedWhitespace, " ")

    @Transactional
    override fun hentGamleSamtykker(olderThan: ZonedDateTime): List<SamtykkeEntity> =
        entityManager.createNativeQuery(hentGamleSamtykkerQuery, SamtykkeEntity::class.java)
            .setParameter("olderThan", olderThan)
            .resultList
            .map { it as SamtykkeEntity }

    private val hentAntallSamtykker =
            """
                SELECT COUNT(*) FROM SAMTYKKE
            """.replace(serieMedWhitespace, " ")

    @Transactional
    override fun hentAntallSamtykker() : Long
            = (entityManager.createNativeQuery(hentAntallSamtykker)
            .singleResult as BigInteger).toLong()


    @Transactional
    override fun hentAntallSamtykkerPerKategori(): Map<String, Long> =
        listOf(
            "PERSONALIA",
            "UTDANNING",
            "FAGBREV",
            "ARBEIDSERFARING",
            "ANNEN_ERFARING",
            "FOERERKORT",
            "LOVREGULERTE_YRKER",
            "OFFENTLIGE_GODKJENNINGER",
            "ANDRE_GODKJENNINGER",
            "KURS",
            "SPRAAK",
            "SAMMENDRAG",
            "KOMPETANSER",
            ).map { kategori ->
                val query = "SELECT COUNT(*) FROM SAMTYKKE WHERE $kategori = true"
                val antall = (entityManager.createNativeQuery(query)
                    .singleResult as BigInteger).toLong()

                Pair(kategori, antall)
            }.toMap()


    private val slettSamtykke =
            """
                DELETE SamtykkeEntity se
                WHERE se.foedselsnummer = :foedselsnummer
                OR se.foedselsnummer like '%:foedselsnummer'
            """.replace(serieMedWhitespace, " ")

    @Transactional
    override fun slettSamtykke(foedselsnummer: String)
            = entityManager.createQuery(slettSamtykke)
                .setParameter("foedselsnummer", foedselsnummer)
                .executeUpdate()

    @Transactional
    override fun oppdaterSamtykke(foedselsnummer: String, samtykke: Samtykke) {
        slettSamtykke(foedselsnummer)
        entityManager.persist(SamtykkeEntity.from(foedselsnummer, samtykke))
    }

    private val finnFoedselsnumre =
            """
                SELECT FOEDSELSNUMMER FROM SAMTYKKE
            """.replace(serieMedWhitespace, " ")

    @Transactional
    override fun finnFoedselsnumre(): List<String>
            = entityManager.createNativeQuery(finnFoedselsnumre)
                .resultList
                .map { it as String }

    private val hentAlleLand =
            """
                SELECT land from SAMTYKKE
            """.replace(serieMedWhitespace, " ")

    @Transactional
    override fun hentAlleLand(): List<String>
        = entityManager.createNativeQuery(hentAlleLand)
        .resultList
        .map { it as String }
}

@Entity
@Table(name = "SAMTYKKE")
class SamtykkeEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "samtykke_generator")
    @SequenceGenerator(name = "samtykke_generator", sequenceName = "SAMTYKKE_SEQ", allocationSize = 1)
    private var id: Long? = null

    @Column(name = "FOEDSELSNUMMER", nullable = false, unique = true)
    lateinit var foedselsnummer: String

    @Column(name = "SIST_ENDRET", nullable = false)
    var sistEndret: ZonedDateTime = ZonedDateTime.now()

    @Column(name = "PERSONALIA", nullable = false)
    var personalia: Boolean = false

    @Column(name = "UTDANNING", nullable = false)
    var utdanning: Boolean = false

    @Column(name = "FAGBREV", nullable = false)
    var fagbrev: Boolean = false

    @Column(name = "ARBEIDSERFARING", nullable = false)
    var arbeidserfaring: Boolean = false

    @Column(name = "ANNEN_ERFARING", nullable = false)
    var annenErfaring: Boolean = false

    @Column(name = "FOERERKORT", nullable = false)
    var foererkort: Boolean = false

    @Column(name = "LOVREGULERTE_YRKER", nullable = false)
    var lovregulerteYrker: Boolean = false

    @Column(name = "OFFENTLIGE_GODKJENNINGER", nullable = false)
    var offentligeGodkjenninger: Boolean = false

    @Column(name = "ANDRE_GODKJENNINGER", nullable = false)
    var andreGodkjenninger: Boolean = false

    @Column(name = "KURS", nullable = false)
    var kurs: Boolean = false

    @Column(name = "SPRAAK", nullable = false)
    var spraak: Boolean = false

    @Column(name = "SAMMENDRAG", nullable = false)
    var sammendrag: Boolean = false

    @Column(name = "KOMPETANSER", nullable = false)
    var kompetanser: Boolean = false

    @Column(name = "LAND", nullable = false)
    var land: String = "[]"


    fun toSamtykke() = Samtykke(
            sistEndret = sistEndret,
            personalia = personalia,
            utdanning = utdanning,
            fagbrev = fagbrev,
            arbeidserfaring = arbeidserfaring,
            annenErfaring = annenErfaring,
            foererkort = foererkort,
            lovregulerteYrker = lovregulerteYrker,
            offentligeGodkjenninger = offentligeGodkjenninger,
            andreGodkjenninger = andreGodkjenninger,
            kurs = kurs,
            spraak = spraak,
            sammendrag = sammendrag,
            kompetanser = kompetanser,
            land = objectMapper.readValue(land, object: TypeReference<List<String>>() {})
    )

    companion object {
        val objectMapper = ObjectMapper()

        fun from(foedselsnummer: String, samtykke: Samtykke): SamtykkeEntity {
            val samtykkeEntity = SamtykkeEntity()
            samtykkeEntity.foedselsnummer = foedselsnummer
            samtykkeEntity.sistEndret = samtykke.sistEndret
            samtykkeEntity.personalia = samtykke.personalia
            samtykkeEntity.utdanning = samtykke.utdanning
            samtykkeEntity.fagbrev = samtykke.fagbrev
            samtykkeEntity.arbeidserfaring = samtykke.arbeidserfaring
            samtykkeEntity.annenErfaring = samtykke.annenErfaring
            samtykkeEntity.foererkort = samtykke.foererkort
            samtykkeEntity.lovregulerteYrker = samtykke.lovregulerteYrker
            samtykkeEntity.offentligeGodkjenninger = samtykke.offentligeGodkjenninger
            samtykkeEntity.andreGodkjenninger = samtykke.andreGodkjenninger
            samtykkeEntity.kurs = samtykke.kurs
            samtykkeEntity.spraak = samtykke.spraak
            samtykkeEntity.sammendrag = samtykke.sammendrag
            samtykkeEntity.kompetanser = samtykke.kompetanser
            samtykkeEntity.land = objectMapper.writeValueAsString(samtykke.land)

            return samtykkeEntity
        }
    }
}
