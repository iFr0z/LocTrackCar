package tk.ifroz.loctrackcar.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PostalCode(
    @Json(name = "PostalCodeNumber")
    val postalCodeNumber: String = ""
)