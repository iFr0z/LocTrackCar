package tk.ifroz.loctrackcar.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marker_car_reminder_table")
data class Reminder(@PrimaryKey @ColumnInfo(name = "reminder") val reminder: String)