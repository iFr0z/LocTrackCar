package tk.ifroz.loctrackcar.data.model.address

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Response(
    @Json(name = "GeoObjectCollection")
    val geoObjectCollection: GeoObjectCollection = GeoObjectCollection()
)