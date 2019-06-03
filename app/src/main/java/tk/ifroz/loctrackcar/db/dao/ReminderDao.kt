package tk.ifroz.loctrackcar.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import tk.ifroz.loctrackcar.db.entity.Reminder

@Dao
abstract class ReminderDao {

    @Query("SELECT reminder FROM marker_car_reminder_table")
    abstract fun getReminder(): LiveData<Reminder>

    @Insert
    abstract suspend fun insert(reminder: Reminder)

    @Query("DELETE FROM marker_car_reminder_table")
    abstract suspend fun delete()

    @Transaction
    open suspend fun upsert(reminder: Reminder) {
        delete()
        insert(reminder)
    }
}