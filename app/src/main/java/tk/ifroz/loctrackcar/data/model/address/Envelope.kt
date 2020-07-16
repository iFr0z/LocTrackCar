package tk.ifroz.loctrackcar.data.model.address

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Envelope(
    @Json(name = "lowerCorner")
    val lowerCorner: String = "",
    @Json(name = "upperCorner")
    val upperCorner: String = ""
)