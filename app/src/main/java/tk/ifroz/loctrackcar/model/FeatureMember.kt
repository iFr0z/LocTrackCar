package tk.ifroz.loctrackcar.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeatureMember(
    @Json(name = "GeoObject")
    val geoObject: GeoObject = GeoObject()
)