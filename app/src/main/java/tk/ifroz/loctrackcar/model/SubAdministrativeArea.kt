package tk.ifroz.loctrackcar.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubAdministrativeArea(
    @Json(name = "Locality")
    val locality: Locality = Locality(),
    @Json(name = "SubAdministrativeAreaName")
    val subAdministrativeAreaName: String = ""
)