package com.example.edward.nyansapo.presentation.ui.add_student

import android.content.SharedPreferences
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.presentation.ui.preassessment.MainRepository
import com.example.edward.nyansapo.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.Exception

class AddStudentViewModel @ViewModelInject constructor(private val repo: MainRepository, private val sharedPref: SharedPreferences) : ViewModel() {

    private val TAG = "AddStudentViewModel"
    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.AddStudent -> {
                    addStudent(event.student)

                }
            }

        }
    }

    private val _addStudentStatus = Channel<Resource<Student>>()
    val addStudentStatus = _addStudentStatus.receiveAsFlow()
    private suspend fun addStudent(student: Student) {
        if (fieldsAreEmpty(student)) {
            _addStudentStatus.send(Resource.error(Exception("Provide all fields")))
        } else {
            postStudent(student)
        }

    }

    private suspend fun postStudent(student: Student) {
        _addStudentStatus.send(Resource.loading("adding student..."))

        try {
            repo.addStudentToGroup(student)
            repo.addStudentToCamp(student)
            _addStudentStatus.send(Resource.success(student))
        } catch (e: Exception) {
            _addStudentStatus.send(Resource.error(e))
        }

    }

    private fun fieldsAreEmpty(student: Student): Boolean {
        student.apply {
            if (firstname!!.isBlank() || lastname!!.isBlank() || age!!.isBlank() || location!!.isBlank() || std_class!!.isBlank()) {
                return true
            }
        }

        return false
    }

    sealed class Event {
        data class AddStudent(val student: Student) : Event()
    }
}