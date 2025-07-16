package tk.ifroz.loctrackcar.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import tk.ifroz.loctrackcar.data.db.dao.ReminderDao
import tk.ifroz.loctrackcar.data.db.dao.TargetDao
import tk.ifroz.loctrackcar.data.db.entity.Reminder
import tk.ifroz.loctrackcar.data.db.entity.Target

@ExperimentalCoroutinesApi
@Database(entities = [Target::class, Reminder::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun targetDao(): TargetDao

    abstract fun reminderDao(): ReminderDao

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
                ).fallbackToDestructiveMigration(false).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}