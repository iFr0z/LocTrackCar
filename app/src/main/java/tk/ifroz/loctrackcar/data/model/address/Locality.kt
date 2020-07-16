package tk.ifroz.loctrackcar.data.model.address

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Locality(
    @Json(name = "LocalityName")
    val localityName: String = "",
    @Json(name = "Thoroughfare")
    val thoroughfare: Thoroughfare = Thoroughfare()
)