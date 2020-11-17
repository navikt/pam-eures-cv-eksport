package no.nav.cv.eures.konverterer.esco

import no.nav.cv.eures.konverterer.esco.dto.CachedEscoMapping
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import javax.persistence.*


interface JanzzCacheRepository {
    fun fetchFromCache(term: String): List<CachedEscoMapping>
    fun fetchFromCacheGreedy(term: String): List<CachedEscoMapping>

    fun saveToCache(cachedEscoMappings: List<CachedEscoMapping>)
    fun pruneCache()
}

@Repository
private class JpaJanzzCacheRepository(
        @PersistenceContext private val entityManager: EntityManager
) : JanzzCacheRepository {
    private val serieMedWhitespace = Regex("(\\s+)")

    companion object {
        val log: Logger = LoggerFactory.getLogger(JanzzCacheRepository::class.java)
    }

    private val fetchFromCache =
            """
                SELECT * FROM ESCO_CACHE
                WHERE TERM = :searchTerm
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun fetchFromCache(term: String) = entityManager.createNativeQuery(fetchFromCache, EscoCacheEntity::class.java)
            .setParameter("searchTerm", term)
            .resultList
            .map { it as EscoCacheEntity }
            .map { it.toCachedEscoMapping() }

    private val fetchFromCacheGreedy =
            """
                SELECT * FROM ESCO_CACHE
                WHERE TERM LIKE :term
            """.replace(serieMedWhitespace, " ")

    @Transactional(readOnly = true)
    override fun fetchFromCacheGreedy(term: String) = entityManager.createNativeQuery(fetchFromCacheGreedy, EscoCacheEntity::class.java)
            .setParameter("term", "%$term%")
            .resultList
            .map { it as EscoCacheEntity }
            .map { it.toCachedEscoMapping() }


    @Transactional
    override fun saveToCache(cachedEscoMappings: List<CachedEscoMapping>) {
        cachedEscoMappings.forEach {
            entityManager.persist(EscoCacheEntity.fromCachedEscoMapping(it))
            log.info("Saved $it to ESCO cache (database)")
        }

    }


    private val pruneCache =
            """
                DELETE EscoCacheEntity
                WHERE UPDATED < :sevenDaysAgo
            """.replace(serieMedWhitespace, " ")

    @Transactional
    override fun pruneCache() {
        entityManager.createQuery(pruneCache)
                .setParameter("sevenDaysAgo", ZonedDateTime.now().minusDays(7))
                .executeUpdate()
    }


}

@Entity
@Table(name = "ESCO_CACHE")
class EscoCacheEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "ESCO_CACHE_SEQ")
    private val id: Long? = null

    @Column(name = "TERM")
    lateinit var term: String

    @Column(name = "CONCEPT_ID")
    lateinit var conceptId: String

    @Column(name = "ESCO")
    lateinit var esco: String

    @Column(name = "UPDATED")
    lateinit var updated: ZonedDateTime

    fun toCachedEscoMapping() = CachedEscoMapping(term, conceptId, esco, updated)

    fun initStatus(term: String, conceptId: String, esco: String, updated: ZonedDateTime): EscoCacheEntity {
        this.term = term
        this.conceptId = conceptId
        this.esco = esco
        this.updated = updated

        return this
    }

    companion object {
        fun fromCachedEscoMapping(cachedEscoMapping: CachedEscoMapping): EscoCacheEntity {
            val entity = EscoCacheEntity()
            return entity.initStatus(cachedEscoMapping.term, cachedEscoMapping.conceptId, cachedEscoMapping.esco, cachedEscoMapping.updated)
        }
    }

}