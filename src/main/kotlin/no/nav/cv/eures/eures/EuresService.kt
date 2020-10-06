package no.nav.cv.eures.eures

import no.nav.cv.eures.cv.CvXmlRepository
import no.nav.cv.eures.eures.AllReferences.Reference
import no.nav.cv.eures.eures.ChangedReferences.ChangedReference
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import javax.inject.Singleton

@Singleton
class EuresService(
        private val cvXmlRepository: CvXmlRepository
) {

    companion object {
        val log = LoggerFactory.getLogger(EuresService::class.java)
    }

    fun getAllReferences() = cvXmlRepository.hentAlle()
            .map { Reference(it) }
            .let { AllReferences(it) }

    fun getChangedReferences(timestamp: Timestamp) = cvXmlRepository.hentAlleEtter(timestamp)
            .partition { it.slettet != null }
            .let {
                val (closed, createdOrModified) = it
                val (created, modified) = createdOrModified.partition { cv -> cv.opprettet.isEqual(cv.sistEndret) }
                ChangedReferences(
                        createdReferences = created.map { cv -> ChangedReference(cv) },
                        modifiedReferences = modified.map { cv -> ChangedReference(cv) },
                        closedReferences = closed.map { cv -> ChangedReference(cv) }
                )
            }
}
