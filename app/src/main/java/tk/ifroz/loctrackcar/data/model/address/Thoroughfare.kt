package tk.ifroz.loctrackcar.data.model.address

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Thoroughfare(
    @Json(name = "Premise")
    val premise: Premise = Premise(),
    @Json(name = "ThoroughfareName")
    val thoroughfareName: String = ""
)