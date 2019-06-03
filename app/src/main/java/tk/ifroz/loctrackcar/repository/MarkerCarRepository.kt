package tk.ifroz.loctrackcar.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import tk.ifroz.loctrackcar.db.dao.ReminderDao
import tk.ifroz.loctrackcar.db.dao.TargetDao
import tk.ifroz.loctrackcar.db.entity.Reminder
import tk.ifroz.loctrackcar.db.entity.Target

class MarkerCarRepository(private val targetDao: TargetDao, private val reminderDao: ReminderDao) {

    val targets: LiveData<Target> = targetDao.getTarget()
    val reminders: LiveData<Reminder> = reminderDao.getReminder()

    @WorkerThread
    suspend fun insertTarget(target: Target) {
        targetDao.insert(target)
    }

    @WorkerThread
    suspend fun upsertReminder(reminder: Reminder) {
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