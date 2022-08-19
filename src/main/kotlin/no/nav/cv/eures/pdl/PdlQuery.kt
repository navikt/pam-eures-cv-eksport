package no.nav.cv.eures.pdl



abstract class PdlQuery {
    abstract val query: String
    abstract val variables: Map<String, String>
}

const val PDL_STATSBORGERSKAP_QUERY = """
    query(${"$"}ident: ID!) {
      hentPerson(ident: ${"$"}ident) {
        statsborgerskap {
          land
          gyldigFraOgMed
          gyldigTilOgMed
        }
      }
    }
"""

data class PdlHentStatsborgerskapQuery(
    override val query: String = PDL_STATSBORGERSKAP_QUERY,
    override val variables: Map<String, String>
) : PdlQuery() {

    constructor(ident: String) : this(
        variables = mapOf("ident" to ident)
    )

}
