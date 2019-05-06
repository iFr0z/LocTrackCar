package tk.ifroz.loctrackcar.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import tk.ifroz.loctrackcar.db.dao.DescriptionDao
import tk.ifroz.loctrackcar.db.dao.ReminderDao
import tk.ifroz.loctrackcar.db.dao.TargetDao
import tk.ifroz.loctrackcar.db.entity.Description
import tk.ifroz.loctrackcar.db.entity.Reminder
import tk.ifroz.loctrackcar.db.entity.Target

class MarkerCarRepository(
    private val targetDao: TargetDao,
    private val reminderDao: ReminderDao,
    private val descriptionDao: DescriptionDao
) {

    val targets: LiveData<Target> = targetDao.getTarget()
    val reminders: LiveData<Reminder> = reminderDao.getReminder()
    val descriptions: LiveData<Description> = descriptionDao.getDescription()

    @WorkerThread
    suspend fun insertTarget(target: Target) {
        targetDao.insert(target)
    }

    @WorkerThread
    suspend fun insertReminder(reminder: Reminder) {
        reminderDao.insert(reminder)
    }

    @WorkerThread
    suspend fun insertDescription(description: Description) {
        descriptionDao.insert(description)
    }

    @WorkerThread
    suspend fun deleteTarget() {
        targetDao.delete()
    }

    @WorkerThread
    suspend fun deleteReminder() {
        reminderDao.delete()
    }

    @WorkerThread
    suspend fun deleteDescription() {
        descriptionDao.delete()
    }
}