package tk.ifroz.loctrackcar.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Address(
    @Json(name = "Components")
    val components: List<Component> = listOf(),
    @Json(name = "country_code")
    val countryCode: String = "",
    @Json(name = "formatted")
    val formatted: String = "",
    @Json(name = "postal_code")
    val postalCode: String = ""
)