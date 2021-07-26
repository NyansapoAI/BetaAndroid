package com.example.edward.nyansapo.numeracy.numeracy_assessment_result.results_list

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.presentation.ui.preassessment.MainRepository
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.cleanString
import com.example.edward.nyansapo.util.formatDate
import com.example.edward.nyansapo.util.studentAttendance
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.lang.StringBuilder

class StudentAssessmentListViewModel @ViewModelInject constructor(private val repository: MainRepository) : ViewModel() {

    private val TAG = "StudentAssessmentListVi"

    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.FetchAssessments -> {
                    fetchAssessments(event.student)
                }
                is Event.FetchAbsenceData -> {
                    fetchAbsenceData(event.student)
                }
            }
        }
    }

    private val _fetchAbsenceDataStatus = Channel<String>()
    val fetchAbsenceDataStatus = _fetchAbsenceDataStatus.receiveAsFlow()
    var sb = StringBuffer()
    var counter = 0
    private suspend fun fetchAbsenceData(student: Student) {
        sb = StringBuffer()
        counter = 0
        val start = DateTime.now().minusDays(10)
        Log.d(TAG, "fetchAbsenceData: start:$start")

        val end = DateTime.now()
        Log.d(TAG, "fetchAbsenceData: end:$end")

        val dateRange: List<DateTime> = getDateRange(start, end)
        dateRange.forEach {
            val date = it.toDate().formatDate.cleanString
            determineAttendanceForThisDate(date, student)
        }
        _fetchAbsenceDataStatus.send(sb.toString())
    }

    private suspend fun determineAttendanceForThisDate(date: String, student: Student) {
        repository.getAttendanceOnce(date).collect {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    val data = it.data!!
                    val count = data.count { it.studentAttendance.present == false && it.id == student.id }
                    if (count > 0) {
                        sb.append(date)
                        sb.append(",")
                        counter++
                    }
                }
            }
        }
    }

    private val _fetchAssessmentsStatus = Channel<Resource<List<DocumentSnapshot>>>()
    val fetchAssessmentsStatus = _fetchAssessmentsStatus.receiveAsFlow()
    private suspend fun fetchAssessments(student: Student) {
        repository.fetchAssessments(student.id!!).collect {
            _fetchAssessmentsStatus.send(it)
        }

    }

    val assessMentsListFlow = MutableStateFlow(listOf<DocumentSnapshot>())
    fun saveAssessMents(data: List<DocumentSnapshot>) {
        assessMentsListFlow.value = data
    }

    fun getDateRange(start: DateTime, end: DateTime): List<DateTime> {
        val ret: MutableList<DateTime> = ArrayList()
        var tmp = start
        while (tmp.isBefore(end) || tmp == end) {
            ret.add(tmp)
            tmp = tmp.plusDays(1)
        }
        return ret
    }

    sealed class Event {
        data class FetchAssessments(val student: Student) : Event()
        data class FetchAbsenceData(val student: Student) : Event()
    }

}