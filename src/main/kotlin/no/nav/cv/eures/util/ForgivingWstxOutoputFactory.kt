package no.nav.cv.eures.util

import com.ctc.wstx.api.InvalidCharHandler
import com.ctc.wstx.api.WstxOutputProperties
import com.ctc.wstx.stax.WstxOutputFactory

// See https://stackoverflow.com/questions/47421275/axis2-error-invalid-white-space-character-0x4-in-text-to-output
class ForgivingWstxOutoputFactory : WstxOutputFactory() {
    fun ForgivingWstxOutoputFactory() {
        setProperty(WstxOutputProperties.P_OUTPUT_INVALID_CHAR_HANDLER,
            InvalidCharHandler.ReplacingHandler(' '))
    }
}