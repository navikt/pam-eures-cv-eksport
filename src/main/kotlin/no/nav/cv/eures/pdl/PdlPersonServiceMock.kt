package no.nav.cv.eures.pdl

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@Service
@Profile("test", "dev")
class PdlPersonServiceMock: PdlPersonGateway {
    override fun erEUEOSstatsborger(ident: String) = true
    override fun getIdenterUtenforEUSomHarSamtykket(identer: List<String>) = listOf("123123123")

}