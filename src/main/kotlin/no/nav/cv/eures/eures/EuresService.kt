package no.nav.cv.eures.eures

import no.nav.cv.eures.cv.CvXml
import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.eures.AllReferences.Reference
import no.nav.cv.eures.eures.ChangedReferences.ChangedReference
import no.nav.cv.eures.eures.CvDetails.CandidateDetail
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.time.ZonedDateTime
import javax.inject.Singleton

@Singleton
class EuresService(
        private val cvXmlRepository: CvXmlRepository
) {

    companion object {
        val log = LoggerFactory.getLogger(EuresService::class.java)
    }

    private fun List<CvXml>.partitionCvs() = partition { it.slettet != null }
            .let {
                val (closed, createdOrModified) = it
                val (created, modified) = createdOrModified.partition { cv -> cv.opprettet.isEqual(cv.sistEndret) }
                return@let Triple(created, modified, closed)
            }


    fun getAllReferences() = cvXmlRepository.hentAlle()
            .map { Reference(it) }
            .let { AllReferences(it) }

    fun getChangedReferences(time: ZonedDateTime) = cvXmlRepository.hentAlleEtter(time)
            .partitionCvs()
            .let {
                val (created, modified, closed) = it
                return@let ChangedReferences(
                        createdReferences = created.map { cv -> ChangedReference(cv) },
                        modifiedReferences = modified.map { cv -> ChangedReference(cv) },
                        closedReferences = closed.map { cv -> ChangedReference(cv) }
                )
            }

    fun getDetails(ids: List<String>) = cvXmlRepository.hentAlle(ids.map(String::toLong))
            .partitionCvs()
            .let {
                val (created, modified, closed) = it
                val map = mutableMapOf<String, CandidateDetail>()
                listOf(created, modified).flatten().forEach { cv ->
                    map["${cv.id}"] = CandidateDetail(
                            creationTimestamp = Timestamp.from(cv.opprettet.toInstant()),
                            lastModificationTimestamp = Timestamp.from(cv.sistEndret.toInstant()),
                            reference = "${cv.id}",
                            status = "ACTIVE",
                            content = cv.xml
                    )
                }

                closed.forEach { cv ->
                    map["${cv.id}"] = CandidateDetail(
                            closingTimestamp = Timestamp.from(cv.slettet?.toInstant()),
                            reference = "${cv.id}",
                            status = "CLOSED"
                    )
                }

                return@let CvDetails(map)
            }
}
