package no.nav.cv.eures

import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.tomcat.autoconfigure.servlet.TomcatServletWebServerAutoConfiguration
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@SpringBootTest(
    classes = [TomcatServletWebServerAutoConfiguration::class, TomcatConfigTest.LocaleCountServlet::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class TomcatConfigTest(@param:LocalServerPort private val port: Int) {

    private val languages = listOf("nb", "nn", "no", "en", "sv", "da", "de", "fr", "es", "fi", "pl", "se")

    @Test
    fun `godtar flere locales i Accept-Language enn Tomcats standardgrense`() {
        val request = HttpRequest.newBuilder(URI("http://localhost:$port/pam-eures-cv-eksport/locales"))
            .header("Accept-Language", languages.joinToString(","))
            .build()

        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(200, response.statusCode())
        assertEquals(languages.size.toString(), response.body())
    }

    class LocaleCountServlet : HttpServlet() {
        override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
            response.writer.write(request.locales.toList().size.toString())
        }
    }
}
