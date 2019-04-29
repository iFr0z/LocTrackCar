package tk.ifroz.loctrackcar.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import tk.ifroz.loctrackcar.db.dao.DescriptionDao
import tk.ifroz.loctrackcar.db.dao.ReminderDao
import tk.ifroz.loctrackcar.db.dao.TargetDao
import tk.ifroz.loctrackcar.db.entity.Description
import tk.ifroz.loctrackcar.db.entity.Reminder
import tk.ifroz.loctrackcar.db.entity.Target

@Database(
    entities = [Target::class, Description::class, Reminder::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun targetDao(): TargetDao

    abstract fun reminderDao(): ReminderDao

    abstract fun descriptionDao(): DescriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DATABASE_NAME = "MarkerCar_db"

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, DATABASE_NAME
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}