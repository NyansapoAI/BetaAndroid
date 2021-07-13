package com.example.edward.nyansapo.presentation.ui.attendance

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.presentation.ui.preassessment.MainRepository
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.cleanString
import com.example.edward.nyansapo.util.formatDate
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

class AttendanceViewModel @ViewModelInject constructor(private val repo: MainRepository) : ViewModel() {

    private val TAG = "AttendanceViewModel"


    private val _attendanceEvents = Channel<Event>()
    val attendanceEvents = _attendanceEvents.receiveAsFlow()
    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.InitDataFetching -> {
                    initDataFetching(event.date)
                }
                is Event.CorrectDateChoosen -> {
                    checkIfWeHaveChoosenCorrectDate(event.date)
                }

            }
        }
    }

    private val _dataFetchingStatus = Channel<Resource<List<DocumentSnapshot>>>()
    val dataFetchingStatus = _dataFetchingStatus.receiveAsFlow()
    private suspend fun initDataFetching(date: Date) {
        _dataFetchingStatus.send(Resource.loading("..."))
        if (attendanceIsEmpty(date)) {
            Log.d(TAG, "initDataFetching:attendance is empty ")
            try {
                getStudentsFromCampAndAddToAttendance(date)
            } catch (e: Exception) {
                //await may throw an error
                e.printStackTrace()
                _dataFetchingStatus.send(Resource.error(e))
            }
        } else {
            getAttendanceContinously(date)
        }

    }

    private suspend fun checkIfWeHaveChoosenCorrectDate(choosenDate: Date) {
        //check if we have choosen a future date and reject it if its future date
///checks if we are on same day

        val currentDateServer = Calendar.getInstance().time
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = choosenDate
        cal2.time = currentDateServer

        val sameDay = cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
                cal1[Calendar.YEAR] == cal2[Calendar.YEAR]
        if (sameDay) {
            _attendanceEvents.send(Event.CorrectDateChoosen(choosenDate))

        } else if (choosenDate.after(currentDateServer)) {
            _attendanceEvents.send(Event.FutureDateChoosen)

        } else {
            _attendanceEvents.send(Event.CorrectDateChoosen(choosenDate))

        }


    }

    private suspend fun getStudentsFromCampAndAddToAttendance(date: Date) {
        Log.d(TAG, "getStudentsFromCampAndAddToAttendance: ")
        val students = repo.getStudentsFromCamp()
        if (students.isEmpty()) {
            _dataFetchingStatus.send(Resource.error(Exception("There are no students in the database!!!")))
        } else {

            startAddingStudentsToAttendance(students, date)

        }
    }

    private suspend fun startAddingStudentsToAttendance(students: List<DocumentSnapshot>, originalDate: Date) {
        Log.d(TAG, "startAddingStudentsToAttendance: ")
        students.forEach {
            val student = it.toObject(Student::class.java)!!
            val studentAttendance = StudentAttendance(student.firstname + " " + student.lastname)
            val date = originalDate.formatDate.cleanString
            repo.addStudentToAttenance(it.id, date, studentAttendance)
        }
        getAttendanceContinously(originalDate)
    }

    private suspend fun getAttendanceContinously(date: Date) {
        Log.d(TAG, "getAttendanceContinously: ")
        val properDate = date.formatDate.cleanString
        _dataFetchingStatus.send(Resource.loading("loading data..."))
        repo.getAttendanceContinuously(properDate).collect {
            _dataFetchingStatus.send(it)
        }

    }

    private suspend fun attendanceIsEmpty(date: Date): Boolean {
        Log.d(TAG, "attendanceIsEmpty: ")
        val dateFormated = date.formatDate.cleanString
        return repo.attendanceIsEmpty(dateFormated)
    }

    sealed class Event {
        data class InitDataFetching(val date: Date) : Event()
        data class CorrectDateChoosen(val date: Date) : Event()
        object FutureDateChoosen : Event()
    }

}