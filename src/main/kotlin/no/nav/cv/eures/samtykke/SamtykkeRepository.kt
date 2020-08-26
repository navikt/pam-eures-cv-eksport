package no.nav.cv.eures.samtykke

import io.micronaut.spring.tx.annotation.Transactional
import no.nav.cv.eures.cv.RawCV
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Singleton
import javax.persistence.*

interface SamtykkeRepository {

    fun hentSamtykke(aktoerId: String) : Samtykke?
    fun slettSamtykke(samtykke: Samtykke)
    fun oppdaterSamtykke(samtykke: Samtykke)
}

@Singleton
private open class JpaSamtykkeRepository(
        @PersistenceContext private val entityManager: EntityManager
) : SamtykkeRepository {
    private val serieMedWhitespace = Regex("(\\s+)")

    private val hentSamtykke =
            """
                SELECT * FROM SAMTYKKE
                WHERE AKTOER_ID = :aktoerId
            """.replace(serieMedWhitespace, " ")

    private val slettSamtykke =
            """
                DELETE FROM SAMTYKKE
                WHERE aktoerId NOT IN
            """


    @Transactional(readOnly = true)
    override fun hentSamtykke(aktoerId: String): Samtykke? {
        return entityManager.createNativeQuery(hentSamtykke, SamtykkeEntity::class.java)
                .setParameter("aktoerId", aktoerId)
                .resultList
                .map { it as SamtykkeEntity }
                .map { it.toSamtykke() }
                .firstOrNull()
    }

    override fun slettSamtykke(samtykke: Samtykke) {
        slettSamtykke
        return entityManager.persist(SamtykkeEntity.from(samtykke))
    }

    override fun oppdaterSamtykke(samtykke: Samtykke) {
        entityManager.persist(SamtykkeEntity.from(samtykke))
    }
}

@Entity
@Table(name = "SAMTYKKE")
class SamtykkeEntity() {
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "SAMTYKKE_SEQ")
    private var id: Long? = null

    @Column(name = "AKTOER_ID", nullable = false, unique = true)
    lateinit var aktoerId: String

    @Column(name = "SIST_ENDRET", nullable = false)
    var sistEndret: ZonedDateTime = ZonedDateTime.now()

    @Column(name = "PERSONALIA", nullable = false)
    var personalia: Boolean = false

    @Column(name = "UTDANNING", nullable = false)
    var utdanning: Boolean = false


    fun toSamtykke() = Samtykke(
            aktoerId = aktoerId,
            sistEndret = sistEndret,
            personalia = personalia,
            utdanning = utdanning
    )

    companion object {
        fun from(samtykke: Samtykke): SamtykkeEntity {
            val samtykkeEntity = SamtykkeEntity()
            samtykkeEntity.aktoerId = samtykke.aktoerId
            samtykkeEntity.sistEndret = samtykke.sistEndret
            samtykkeEntity.personalia = samtykke.personalia
            samtykkeEntity.utdanning = samtykke.utdanning
            return samtykkeEntity
        }
    }
}