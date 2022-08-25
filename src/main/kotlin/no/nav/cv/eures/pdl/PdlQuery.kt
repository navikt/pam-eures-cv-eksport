package no.nav.cv.eures.pdl



abstract class PdlQuery {
    abstract val query: String
    abstract val variables: Map<String, String>
}
abstract class PdlQueryMultiple {
    abstract val query: String
    abstract val variables: Map<String, List<String>>
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

const val PDL_STATSBORGERSKAP_LISTE_QUERY = """
    query(${"$"}identer: [ID!]!) {
      hentPersonBolk(identer: ${"$"}identer) {
        ident
        person {
          statsborgerskap {
            land
            gyldigFraOgMed
            gyldigTilOgMed
          }
        },
        code
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

}data class PdlHentStatsborgerskapListeQuery(
    override val query: String = PDL_STATSBORGERSKAP_LISTE_QUERY,
    override val variables: Map<String, List<String>>
) : PdlQueryMultiple() {

    constructor(identer: List<String>) : this(
        //variables = mapOf("identer" to "[${identer.map{"\"${it}\""}.joinToString ( "," )}]")
        variables = mapOf("identer" to identer)
    )

}
