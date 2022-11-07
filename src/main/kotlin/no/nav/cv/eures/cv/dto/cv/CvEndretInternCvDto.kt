package no.nav.cv.dto.cv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternCvDto(
    val uuid: UUID?,
    val hasCar: Boolean?,
    val summary: String?,
    val otherExperience: List<CvEndretInternOtherExperience>,
    val workExperience: List<CvEndretInternWorkExperience>,
    val courses: List<CvEndretInternCourse>,
    val certificates: List<CvEndretInternCertificate>,
    val languages: List<CvEndretInternLanguage>,
    val education: List<CvEndretInternEducation>,
    val vocationalCertificates: List<CvEndretInternVocationalCertificate>,
    val authorizations: List<CvEndretInternAuthorization>,
    val driversLicenses: List<CvEndretInternDriversLicence>,
    val skillDrafts: List<CvEndretInternSkillDraft>,
    val synligForArbeidsgiver: Boolean,
    val synligForVeileder: Boolean,
    val createdAt: ZonedDateTime?,
    val updatedAt: ZonedDateTime?
)