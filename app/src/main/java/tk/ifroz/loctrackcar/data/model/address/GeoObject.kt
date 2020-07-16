package tk.ifroz.loctrackcar.data.model.address

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeoObject(
    @Json(name = "Point")
    val point: Point = Point(),
    @Json(name = "boundedBy")
    val boundedBy: BoundedBy = BoundedBy(),
    @Json(name = "description")
    val description: String = "",
    @Json(name = "metaDataProperty")
    val metaDataProperty: MetaDataProperty = MetaDataProperty(),
    @Json(name = "name")
    val name: String = ""
)