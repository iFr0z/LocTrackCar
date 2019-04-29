package tk.ifroz.loctrackcar.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MetaDataPropertyX(
    @Json(name = "GeocoderResponseMetaData")
    val geocoderResponseMetaData: GeocoderResponseMetaData = GeocoderResponseMetaData()
)