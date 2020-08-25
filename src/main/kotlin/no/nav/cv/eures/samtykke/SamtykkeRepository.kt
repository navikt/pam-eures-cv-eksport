package no.nav.cv.eures.samtykke

import io.micronaut.spring.tx.annotation.Transactional
import no.nav.cv.eures.cv.RawCV
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Singleton
import javax.persistence.*

interface SamtykkeRepository {

    fun hentSamtykke(aktoerId: String) : Samtykke?
    fun slettSamtykke(aktoerId: String)
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

    @Transactional(readOnly = true)
    override fun hentSamtykke(aktoerId: String): Samtykke? {
        return entityManager.createNativeQuery(hentSamtykke, Samtykke::class.java)
                .setParameter("aktoerId", aktoerId)
                .resultList
                .map { it as SamtykkeEntity }
                .map { it.toSamtykke() }
                .firstOrNull()
    }

    override fun slettSamtykke(aktoerId: String) {
        TODO("Not yet implemented")
    }

    override fun oppdaterSamtykke(samtykke: Samtykke) {
        TODO("Not yet implemented")
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
    lateinit var sistEndret: ZonedDateTime

    @Column(name = "PERSONALIA", nullable = false)
    lateinit var personalia: Boolean

    @Column(name = "UTDANNING", nullable = false)
    lateinit var utdanning: Boolean


    fun toSamtykke() = Samtykke(
            aktoerId = aktoerId,
            personalia = personalia,
            utdanning = utdanning
    )
}