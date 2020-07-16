package tk.ifroz.loctrackcar.data.model.address

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Component(
    @Json(name = "kind")
    val kind: String = "",
    @Json(name = "name")
    val name: String = ""
)