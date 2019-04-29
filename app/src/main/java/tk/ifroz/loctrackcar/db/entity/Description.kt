package tk.ifroz.loctrackcar.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marker_car_description_table")
data class Description(@PrimaryKey @ColumnInfo(name = "description") val description: String)