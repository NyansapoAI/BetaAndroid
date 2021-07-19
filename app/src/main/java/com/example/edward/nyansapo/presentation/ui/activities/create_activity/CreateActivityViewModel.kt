package com.example.edward.nyansapo.presentation.ui.activities.create_activity

import android.content.SharedPreferences
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.presentation.ui.activities.Activity
import com.example.edward.nyansapo.presentation.ui.preassessment.MainRepository
import com.example.edward.nyansapo.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.Exception

class CreateActivityViewModel @ViewModelInject constructor(private val repo: MainRepository) : ViewModel() {

    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.CreateActivity -> {
                    createActivity(event.activity)
                }
            }
        }
    }

    private val _saveActivityStatus = Channel<Resource<Activity>>()
    val saveActivityStatus = _saveActivityStatus.receiveAsFlow()
    private suspend fun createActivity(activity: Activity) {
        if (!fieldsIsEmpty(activity)) {
            saveActivity(activity)
        } else {
            _saveActivityStatus.send(Resource.error(Exception("Please Fill All Fields")))
        }

    }

    private suspend fun saveActivity2(activity: Activity) {
        try {
            _saveActivityStatus.send(Resource.loading("saving activity..."))
            repo.saveActivity(activity)
            _saveActivityStatus.send(Resource.success(activity))
        } catch (e: Exception) {
            _saveActivityStatus.send(Resource.error(e))

        }
    }
    private suspend fun saveActivity(activity: Activity) {


        repo.saveActivity2(activity).collect {
            _saveActivityStatus.send(it)
        }

    }

    private fun fieldsIsEmpty(activity: Activity): Boolean {
        return activity.name.isBlank() || activity.steps.isBlank()

    }

    sealed class Event {
        data class CreateActivity(val activity: Activity) : Event()
    }
}