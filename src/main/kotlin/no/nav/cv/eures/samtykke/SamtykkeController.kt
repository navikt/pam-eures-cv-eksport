package no.nav.cv.eures.samtykke

import com.auth0.jwt.JWT
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("samtykke")
class SamtykkeController(
        private val samtykkeService: SamtykkeService
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(SamtykkeController::class.java)
    }

    // TODO - Actual verification of token, this is just a temporary unwrapping.
    @Get(produces = ["application/json"])
    fun hentSamtykke(@Header("Authorization") bearerToken: String) =
            bearerToken.decode()?.let {
                samtykkeService.hentSamtykke(it)
                    ?: HttpResponse.notFound<String>()
            }


    @Post(produces = ["application/json"])
    fun oppdaterSamtykke(
            @Header("Authorization") bearerToken: String,
            @Body samtykke: Samtykke
    ): HttpResponse<Samtykke> {
        bearerToken.decode()?.let {
            samtykkeService.oppdaterSamtykke(it, samtykke)
            return HttpResponse.ok()
        }
        return HttpResponse.notFound()
    }

    @Delete(produces = ["application/json"])
    fun slettSamtykke(@Header("Authorization") bearerToken: String): HttpResponse<String> {
        bearerToken.decode()?.let {
            samtykkeService.slettSamtykke(it)
            return HttpResponse.ok("OK")
        }
        return HttpResponse.notFound()
    }

    private fun String?.decode(): String? = this?.let {
        val token = it.split(" ")[1]
        JWT.decode(token).subject
    }

}
