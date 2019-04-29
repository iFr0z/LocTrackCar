package tk.ifroz.loctrackcar.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MetaDataProperty(
    @Json(name = "GeocoderMetaData")
    val geocoderMetaData: GeocoderMetaData = GeocoderMetaData()
)