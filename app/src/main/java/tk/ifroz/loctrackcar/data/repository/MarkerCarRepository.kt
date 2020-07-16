package tk.ifroz.loctrackcar.data.repository

import androidx.annotation.WorkerThread
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import tk.ifroz.loctrackcar.data.db.dao.ReminderDao
import tk.ifroz.loctrackcar.data.db.dao.TargetDao
import tk.ifroz.loctrackcar.data.db.entity.Reminder
import tk.ifroz.loctrackcar.data.db.entity.Target

@ExperimentalCoroutinesApi
class MarkerCarRepository(private val targetDao: TargetDao, private val reminderDao: ReminderDao) {

    val targets: Flow<Target?> = targetDao.getTarget()
    val reminders: Flow<Reminder?> = reminderDao.getReminder()

    @WorkerThread
    suspend fun insertTarget(target: Target?) {
        targetDao.insert(target)
    }

    @WorkerThread
    suspend fun upsertReminder(reminder: Reminder?) {
        reminderDao.upsert(reminder)
    }

    @WorkerThread
    suspend fun deleteTarget() {
        targetDao.delete()
    }

    @WorkerThread
    suspend fun deleteReminder() {
        reminderDao.delete()
    }
}