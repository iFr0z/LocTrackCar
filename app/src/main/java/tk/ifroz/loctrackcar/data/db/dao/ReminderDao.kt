package tk.ifroz.loctrackcar.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import tk.ifroz.loctrackcar.data.db.entity.Reminder

@ExperimentalCoroutinesApi
@Dao
abstract class ReminderDao {

    @Query("SELECT reminder FROM marker_car_reminder_table")
    abstract fun getReminder(): Flow<Reminder?>

    @Insert
    abstract suspend fun insert(reminder: Reminder)

    @Query("DELETE FROM marker_car_reminder_table")
    abstract suspend fun delete()

    @Transaction
    open suspend fun upsert(reminder: Reminder?) {
        delete()
        insert(reminder!!)
    }
}