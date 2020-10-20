package no.nav.cv.eures.cv

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("internal/cvConsumer")
class CvConsumerAdmin(
        private val cvConsumer: CvConsumer
) {

    @Get("seekToBeginning", produces = ["text/plain"])
    fun seekToBeginning() : String {
        cvConsumer.seekToBeginning()
        return "OK"
    }
}