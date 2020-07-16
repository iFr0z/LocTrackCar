package tk.ifroz.loctrackcar.data.model.address

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Premise(
    @Json(name = "PostalCode")
    val postalCode: PostalCode = PostalCode(),
    @Json(name = "PremiseNumber")
    val premiseNumber: String = ""
)