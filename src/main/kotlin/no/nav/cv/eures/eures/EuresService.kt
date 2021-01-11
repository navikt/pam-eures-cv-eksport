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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Component
class EuresService(
        private val cvXmlRepository: CvXmlRepository
) {

    companion object {
        val log: Logger= LoggerFactory.getLogger(EuresService::class.java)
    }

    private fun List<CvXml>.partitionCvs() = partition { it.slettet != null }
            .let {
                val (closed, createdOrModified) = it
                val (created, modified) = createdOrModified.partition { cv -> cv.opprettet.isEqual(cv.sistEndret) }
                return@let Triple(created, modified, closed)
            }

    fun getAll() = cvXmlRepository.fetchAll().partitionCvs()

    fun getAllReferences() = cvXmlRepository.fetchAllActive()
            .map { Reference(it) }
            .let { GetAllReferences(it) }

    fun getChangedReferences(time: ZonedDateTime) = cvXmlRepository.fetchAllCvsAfterTimestamp(time)
            .partitionCvs()
            .let {
                val (created, modified, closed) = it
                return@let GetChangedReferences(
                        createdReferences = created.map { cv -> ChangedReference(cv) },
                        modifiedReferences = modified.map { cv -> ChangedReference(cv) },
                        closedReferences = closed.map { cv -> ChangedReference(cv) }
                )
            }

    fun getDetails(references: List<String>) = cvXmlRepository.fetchAllCvsByReference(references)
            .partitionCvs()
            .let {
                val (created, modified, closed) = it
                val map = mutableMapOf<String, CandidateDetail>()
                listOf(created, modified).flatten().forEach { cv ->
                    map[cv.reference] = CandidateDetail(
                            creationTimestamp = cv.opprettet.toInstant().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli(),
                            lastModificationTimestamp = cv.sistEndret.toInstant().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli(),
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
}
