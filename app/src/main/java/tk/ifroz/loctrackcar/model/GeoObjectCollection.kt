package tk.ifroz.loctrackcar.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeoObjectCollection(
    @Json(name = "featureMember")
    val featureMember: List<FeatureMember> = listOf(),
    @Json(name = "metaDataProperty")
    val metaDataProperty: MetaDataProperty = MetaDataProperty()
)