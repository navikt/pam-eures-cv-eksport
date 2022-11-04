package no.nav.cv.dto.jobwishes

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class CvEndretInternJobwishesDto(
    val id: Long,
    val startOption: String?,
    val occupations: List<CvEndretInternOccupation>,
    val occupationDrafts: List<CvEndretInternOccupationDraft>,
    val skills: List<CvEndretInternSkill>,
    val locations: List<CvEndretInternLocation>,
    val occupationTypes: List<CvEndretInternOccupationType>,
    val workTimes: List<CvEndretInternWorkTime>,
    val workDays: List<CvEndretInternWorkDay>,
    val workShiftTypes: List<CvEndretInternWorkShiftType>,
    val workLoadTypes: List<CvEndretInternWorkLoadType>,
    val createdAt: ZonedDateTime?,
    val updatedAt: ZonedDateTime?,
)