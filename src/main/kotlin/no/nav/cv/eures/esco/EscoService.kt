package no.nav.cv.eures.esco

import no.nav.cv.eures.esco.dto.EscoConceptDto
import no.nav.cv.eures.esco.dto.EscoKodeType.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service

@Service
class EscoService(private val ontologiClient: OntologiClient) : InitializingBean {
    companion object {
        private lateinit var instance: EscoService
        fun instance() = instance
        val log = LoggerFactory.getLogger(EscoService::class.java)
        val iscoPrefix = "http://data.europa.eu/esco/isco/c"
    }

    override fun afterPropertiesSet() {
        instance = this
    }

    fun hentEscoForKonseptId(konseptId: String): List<EscoConceptDto> {
        val konseptGruppering = ontologiClient.hentKonseptGrupperingFraOntologien(konseptId)
        val esco = konseptGruppering.esco

        if (esco == null || esco.uri.isEmpty()) {
            log.warn("Fant ingen escokode for konseptId $konseptId ved oppslag mot pam-ontologi")
            return emptyList()
        }

        return esco.uri.split("|").map {
            val type = if (it.contains("/isco/")) ISCO else ESCO
            val kode = if (type == ESCO) it else it.lowercase().removePrefix(iscoPrefix)

            EscoConceptDto(
                label = esco.label ?: "",
                kode = kode,
                type = type
            )
        }
    }
}