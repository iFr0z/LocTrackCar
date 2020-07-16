package tk.ifroz.loctrackcar.data.model.address

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Country(
    @Json(name = "AddressLine")
    val addressLine: String = "",
    @Json(name = "AdministrativeArea")
    val administrativeArea: AdministrativeArea = AdministrativeArea(),
    @Json(name = "CountryName")
    val countryName: String = "",
    @Json(name = "CountryNameCode")
    val countryNameCode: String = ""
)