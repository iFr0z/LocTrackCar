package tk.ifroz.loctrackcar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import tk.ifroz.loctrackcar.db.AppDatabase
import tk.ifroz.loctrackcar.db.entity.Description
import tk.ifroz.loctrackcar.db.entity.Reminder
import tk.ifroz.loctrackcar.db.entity.Target
import tk.ifroz.loctrackcar.repository.MarkerCarRepository

class MarkerCarViewModel(application: Application) : AndroidViewModel(application) {

    private var parentJob = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("Caught original $exception")
    }
    private val scope = CoroutineScope(parentJob + Dispatchers.Main + exceptionHandler)

    private val repository: MarkerCarRepository
    val targets: LiveData<Target>
    val reminders: LiveData<Reminder>
    val descriptions: LiveData<Description>

    init {
        val targetsDao = AppDatabase.getDatabase(application).targetDao()
        val remindersDao = AppDatabase.getDatabase(application).reminderDao()
        val descriptionsDao = AppDatabase.getDatabase(application).descriptionDao()
        repository = MarkerCarRepository(
            targetsDao,
            remindersDao,
            descriptionsDao
        )
        targets = repository.targets
        reminders = repository.reminders
        descriptions = repository.descriptions
    }

    fun insertTarget(target: Target) = scope.launch(Dispatchers.IO) {
        repository.insertTarget(target)
    }

    fun insertReminder(reminder: Reminder) = scope.launch(Dispatchers.IO) {
        repository.insertReminder(reminder)
    }

    fun insertDescription(description: Description) = scope.launch(Dispatchers.IO) {
        repository.insertDescription(description)
    }

    fun deleteTarget() = scope.launch(Dispatchers.IO) {
        repository.deleteTarget()
    }

    fun deleteReminder() = scope.launch(Dispatchers.IO) {
        repository.deleteReminder()
    }

    fun deleteDescription() = scope.launch(Dispatchers.IO) {
        repository.deleteDescription()
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}