package tk.ifroz.loctrackcar.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AdministrativeArea(
    @Json(name = "AdministrativeAreaName")
    val administrativeAreaName: String = "",
    @Json(name = "SubAdministrativeArea")
    val subAdministrativeArea: SubAdministrativeArea = SubAdministrativeArea()
)