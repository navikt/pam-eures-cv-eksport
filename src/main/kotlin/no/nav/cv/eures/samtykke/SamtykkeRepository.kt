package no.nav.cv.eures.samtykke

interface SamtykkeRepository {

    fun hentSamtykke(aktoerId: String) : Samtykke
    fun slettSamtykke(aktoerId: String)
    fun oppdaterSamtykke(samtykke: Samtykke)
}