package tk.ifroz.loctrackcar.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeocoderMetaData(
    @Json(name = "Address")
    val address: Address = Address(),
    @Json(name = "AddressDetails")
    val addressDetails: AddressDetails = AddressDetails(),
    @Json(name = "kind")
    val kind: String = "",
    @Json(name = "precision")
    val precision: String = "",
    @Json(name = "text")
    val text: String = ""
)