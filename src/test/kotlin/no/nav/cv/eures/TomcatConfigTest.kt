package no.nav.cv.eures

import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TomcatConfigTest {

    private val languages = listOf("nb", "nn", "no", "en", "sv", "da", "de", "fr", "es", "fi", "pl", "se")

    @Test
    fun `godtar flere locales i Accept-Language enn Tomcats standardgrense`() {
        val factory = TomcatServletWebServerFactory(0)
        TomcatConfig().unlimitedLocaleCount().customize(factory)
        val server = factory.getWebServer({ context ->
            context.addServlet("locales", LocaleCountServlet()).addMapping("/locales")
        })
        server.start()
        try {
            val request = HttpRequest.newBuilder(URI("http://localhost:${server.port}/locales"))
                .header("Accept-Language", languages.joinToString(","))
                .build()

            val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

            assertEquals(200, response.statusCode())
            assertEquals(languages.size.toString(), response.body())
        } finally {
            server.stop()
        }
    }

    private class LocaleCountServlet : HttpServlet() {
        override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
            response.writer.write(request.locales.toList().size.toString())
        }
    }
}
