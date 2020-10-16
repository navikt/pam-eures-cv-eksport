package no.nav.cv.eures.samtykke

import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import javax.inject.Singleton
import javax.persistence.*

interface SamtykkeRepository {

    fun hentSamtykke(foedselsnummer: String) : Samtykke?
    fun hentSamtykkeUtenNaaverendeXml(foedselsnummer: List<String>) : List<SamtykkeEntity>
    fun slettSamtykke(foedselsnummer: String) : Int
    fun oppdaterSamtykke(samtykke: Samtykke)

}

@Singleton
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

    private val slettSamtykke =
            """
                DELETE SamtykkeEntity se
                WHERE se.foedselsnummer = :foedselsnummer
            """.replace(serieMedWhitespace, " ")

    @Transactional
    override fun slettSamtykke(foedselsnummer: String)
            = entityManager.createQuery(slettSamtykke)
                .setParameter("foedselsnummer", foedselsnummer)
                .executeUpdate()

    @Transactional
    override fun oppdaterSamtykke(samtykke: Samtykke) {
        slettSamtykke(samtykke.foedselsnummer)
        entityManager.persist(SamtykkeEntity.from(samtykke))
    }
}

@Entity
@Table(name = "SAMTYKKE")
class SamtykkeEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "SAMTYKKE_SEQ")
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

    @Column(name = "ANDRE_GODKJENNINGER", nullable = false)
    var andreGodkjenninger: Boolean = false

    @Column(name = "KURS", nullable = false)
    var kurs: Boolean = false

    @Column(name = "SPRAAK", nullable = false)
    var spraak: Boolean = false

    @Column(name = "SAMMENDRAG", nullable = false)
    var sammendrag: Boolean = false


    fun toSamtykke() = Samtykke(
            foedselsnummer = foedselsnummer,
            sistEndret = sistEndret,
            personalia = personalia,
            utdanning = utdanning,
            fagbrev = fagbrev,
            arbeidserfaring = arbeidserfaring,
            annenErfaring = annenErfaring,
            foererkort = foererkort,
            lovregulerteYrker = lovregulerteYrker,
            andreGodkjenninger = andreGodkjenninger,
            kurs = kurs,
            spraak = spraak,
            sammendrag = sammendrag
    )

    companion object {
        fun from(samtykke: Samtykke): SamtykkeEntity {
            val samtykkeEntity = SamtykkeEntity()
            // TODO - Remove from DTO
            samtykkeEntity.foedselsnummer = samtykke.foedselsnummer
            samtykkeEntity.sistEndret = samtykke.sistEndret
            samtykkeEntity.personalia = samtykke.personalia
            samtykkeEntity.utdanning = samtykke.utdanning
            samtykkeEntity.fagbrev = samtykke.fagbrev
            samtykkeEntity.arbeidserfaring = samtykke.arbeidserfaring
            samtykkeEntity.annenErfaring = samtykke.annenErfaring
            samtykkeEntity.foererkort = samtykke.foererkort
            samtykkeEntity.lovregulerteYrker = samtykke.lovregulerteYrker
            samtykkeEntity.andreGodkjenninger = samtykke.andreGodkjenninger
            samtykkeEntity.kurs = samtykke.kurs
            samtykkeEntity.spraak = samtykke.spraak
            samtykkeEntity.sammendrag = samtykke.sammendrag
            return samtykkeEntity
        }
    }
}
