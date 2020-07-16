package tk.ifroz.loctrackcar.data.model.address

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeocoderResponseMetaData(
    @Json(name = "Point")
    val point: Point = Point(),
    @Json(name = "found")
    val found: String = "",
    @Json(name = "request")
    val request: String = "",
    @Json(name = "results")
    val results: String = ""
)