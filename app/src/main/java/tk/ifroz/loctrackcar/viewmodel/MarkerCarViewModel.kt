package tk.ifroz.loctrackcar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tk.ifroz.loctrackcar.db.AppDatabase
import tk.ifroz.loctrackcar.db.entity.Reminder
import tk.ifroz.loctrackcar.db.entity.Target
import tk.ifroz.loctrackcar.repository.MarkerCarRepository

class MarkerCarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MarkerCarRepository
    val targets: LiveData<Target>
    val reminders: LiveData<Reminder>

    init {
        val targetsDao = AppDatabase.getDatabase(application).targetDao()
        val remindersDao = AppDatabase.getDatabase(application).reminderDao()
        repository = MarkerCarRepository(targetsDao, remindersDao)
        targets = repository.targets
        reminders = repository.reminders
    }

    fun insertTarget(target: Target) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertTarget(target)
    }

    fun insertReminder(reminder: Reminder) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertReminder(reminder)
    }

    fun deleteTarget() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTarget()
    }

    fun deleteReminder() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteReminder()
    }
}