package no.nav.cv.eures.eures

import no.nav.cv.eures.cv.CvXml
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.eures.dto.GetAllReferences
import no.nav.cv.eures.eures.dto.GetAllReferences.Reference
import no.nav.cv.eures.eures.dto.GetChangedReferences
import no.nav.cv.eures.eures.dto.GetChangedReferences.ChangedReference
import no.nav.cv.eures.eures.dto.GetDetails
import no.nav.cv.eures.eures.dto.GetDetails.CandidateDetail
import no.nav.cv.eures.eures.dto.GetDetails.CandidateDetail.Status.ACTIVE
import no.nav.cv.eures.eures.dto.GetDetails.CandidateDetail.Status.CLOSED
import no.nav.cv.eures.pdl.PdlPersonGateway
import no.nav.cv.eures.samtykke.SamtykkeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Component
class EuresService(
    private val cvXmlRepository: CvXmlRepository,
    private val samtykkeRepository: SamtykkeRepository,
    private val personGateway: PdlPersonGateway
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(EuresService::class.java)
    }

    private fun List<CvXml>.partitionCvs() = partition { it.slettet != null }
        .let {
            val (closed, createdOrModified) = it
            val (created, modified) = createdOrModified.partition { cv -> cv.opprettet.isEqual(cv.sistEndret) }
            return@let Triple(created, modified, closed)
        }

    fun getAllCounts(): Triple<Long, Long, Long> {
        val created = cvXmlRepository.fetchCountCreated()
        val modified = cvXmlRepository.fetchCountModified()
        val closed = cvXmlRepository.fetchCountClosed()
        return Triple(created, modified, closed)
    }

    fun getAllReferences(): GetAllReferences {

        var references = listOf<Reference>()
        var pageRequest: Pageable = PageRequest.of(0, 100)

        do {
            val page = cvXmlRepository.fetchAllActive(pageRequest)
            references = references.plus(page.content.map { cv -> Reference(cv) })
            pageRequest = pageRequest.next()
        } while (!page.isLast)

        log.info("EURES Controller fetching all ${references.size} CVs ")

        return GetAllReferences(references)

    }

    fun getChangedReferences(time: ZonedDateTime): GetChangedReferences {
        var createdReferences = listOf<ChangedReference>()
        var modifiedReferences = listOf<ChangedReference>()
        var closedReferences = listOf<ChangedReference>()
        var pageRequest: Pageable = PageRequest.of(0, 100)
        do {
            val page = cvXmlRepository.fetchAllCvsAfterTimestamp(pageRequest, time)
            page.content.partitionCvs()
                .also { (created, modified, closed) ->
                    log.info(
                        "EURES Controller has these changed references: \n" +
                                "${created.size} created : ${created.joinToString { it.reference }}\n" +
                                "${modified.size} modified : ${modified.joinToString { it.reference }}\n" +
                                "${closed.size} closed : ${closed.joinToString { it.reference }}"
                    )
                }
                .let { (created, modified, closed) ->
                    createdReferences = closedReferences.plus(created.map { cv -> ChangedReference(cv) })
                    modifiedReferences = modifiedReferences.plus(modified.map { cv -> ChangedReference(cv) })
                    closedReferences = closedReferences.plus(closed.map { cv -> ChangedReference(cv) })
                }
            pageRequest = pageRequest.next()
        } while (!page.isLast)
        return GetChangedReferences(
            createdReferences = createdReferences,
            modifiedReferences = modifiedReferences,
            closedReferences = closedReferences,
        )
    }


    fun getDetails(references: List<String>) = cvXmlRepository.fetchAllCvsByReference(references)
        .partitionCvs()
        .also { (created, modified, closed) ->
            log.info(
                "EURES Controller getDetails for " +
                        "${created.size} created, ${modified.size} modified, ${closed.size} closed CVs"
            )
        }
        .let { (created, modified, closed) ->
            val map = mutableMapOf<String, CandidateDetail>()
            listOf(created, modified).flatten().forEach { cv ->
                map[cv.reference] = CandidateDetail(
                    creationTimestamp = cv.opprettet.toInstant().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli(),
                    lastModificationTimestamp = cv.sistEndret.toInstant().atOffset(ZoneOffset.UTC).toInstant()
                        .toEpochMilli(),
                    reference = cv.reference,
                    status = ACTIVE,
                    content = cv.xml
                )
            }

            closed.forEach { cv ->
                map[cv.reference] = CandidateDetail(
                    closingTimestamp = cv.slettet?.toInstant()?.atOffset(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
                    reference = cv.reference,
                    status = CLOSED
                )
            }

            return@let GetDetails(map)
        }

    fun getAntallIdenterUtenforEUSomHarSamtykket() : Int {
        val identer = samtykkeRepository.finnFoedselsnumre()

        val resultat = personGateway.getIdenterUtenforEUSomHarSamtykket(identer)

        return resultat?.size ?: error("Antall identer kunne ikke hentes ut")
    }
}
