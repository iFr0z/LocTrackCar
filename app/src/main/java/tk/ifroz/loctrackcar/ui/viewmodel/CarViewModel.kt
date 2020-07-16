package tk.ifroz.loctrackcar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import tk.ifroz.loctrackcar.data.db.AppDatabase
import tk.ifroz.loctrackcar.data.db.entity.Reminder
import tk.ifroz.loctrackcar.data.db.entity.Target
import tk.ifroz.loctrackcar.data.repository.MarkerCarRepository

@ExperimentalCoroutinesApi
class CarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MarkerCarRepository
    val targets: Flow<Target?>
    val reminders: Flow<Reminder?>

    init {
        val targetsDao = AppDatabase.getDatabase(application).targetDao()
        val remindersDao = AppDatabase.getDatabase(application).reminderDao()
        repository = MarkerCarRepository(targetsDao, remindersDao)
        targets = repository.targets
        reminders = repository.reminders
    }

    fun insertTarget(target: Target?) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertTarget(target)
    }

    fun upsertReminder(reminder: Reminder?) = viewModelScope.launch(Dispatchers.IO) {
        repository.upsertReminder(reminder)
    }

    fun deleteTarget() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTarget()
    }

    fun deleteReminder() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteReminder()
    }
}