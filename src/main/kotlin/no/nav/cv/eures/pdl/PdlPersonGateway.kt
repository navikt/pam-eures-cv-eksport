package no.nav.cv.eures.pdl

interface PdlPersonGateway {
    fun erEUEOSstatsborger(ident: String): Boolean?
    fun getIdenterUtenforEUSomHarSamtykket(identer: List<String>) : List<String>?
}