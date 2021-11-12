package no.nav.cv.eures.preview

import no.nav.cv.eures.model.Candidate

class PreviewDto(
    val candidate: Candidate? = null,
    val escoMap: Map<String, String?>? = null
)