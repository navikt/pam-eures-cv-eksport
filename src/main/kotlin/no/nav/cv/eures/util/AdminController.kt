package no.nav.cv.eures.util

import no.nav.cv.eures.konverterer.CvConverterService
import no.nav.cv.eures.scheduled.XmlUpdater
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
@RequestMapping("internal/reprocess")
class AdminController(
    private val xmlUpdater: XmlUpdater,
    private val cvConverterService2: CvConverterService,
) {

    companion object {
        private val log = LoggerFactory.getLogger(AdminController::class.java)
    }

    @GetMapping("single/{fnr}", produces = [ "text/plain" ])
    fun single(@PathVariable("fnr") fnr: String) : String {

        log.info("Admin interface triggering reprocessing of CV " +
                "${fnr.take(1)}.........${fnr.takeLast(1)}")

        cvConverterService2.createOrUpdate(fnr)

        return "DONE"
    }

    @GetMapping("all", produces = [ "text/plain" ])
    fun all() : String {

        log.info("Admin interface triggering reprocessing of all CVs")

        xmlUpdater.updateXmlCv()

        return "DONE"
    }
}