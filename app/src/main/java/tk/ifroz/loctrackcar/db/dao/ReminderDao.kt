package tk.ifroz.loctrackcar.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import tk.ifroz.loctrackcar.db.entity.Reminder

@Dao
interface ReminderDao {

    @Query("SELECT reminder FROM marker_car_reminder_table")
    fun getReminder(): LiveData<Reminder>

    @Insert
    suspend fun insert(reminder: Reminder)

    @Query("DELETE FROM marker_car_reminder_table")
    suspend fun delete()
}