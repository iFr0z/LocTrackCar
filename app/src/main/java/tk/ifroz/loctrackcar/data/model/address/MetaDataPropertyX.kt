package tk.ifroz.loctrackcar.data.model.address

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MetaDataPropertyX(
    @Json(name = "GeocoderResponseMetaData")
    val geocoderResponseMetaData: GeocoderResponseMetaData = GeocoderResponseMetaData()
)