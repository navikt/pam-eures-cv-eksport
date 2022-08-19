package no.nav.cv.eures.pdl

interface PdlPersonGateway {
    fun erEUEOSstatsborger(ident: String): Boolean?
}