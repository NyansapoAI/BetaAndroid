package com.example.edward.nyansapo.numeracy.numeracy_assessment_result.results_list

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.numeracy.numeracy_assessment_result.NumeracyAssessmentResultViewModel
import com.example.edward.nyansapo.presentation.ui.assessment.BeginAssessmentFragment
import com.example.edward.nyansapo.presentation.ui.preassessment.MainRepository
import com.example.edward.nyansapo.util.Resource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class StudentAssessmentListViewModel @ViewModelInject constructor(private val repository: MainRepository) : ViewModel() {

    fun setEvent(event:Event){
        viewModelScope.launch {
            when(event){
                is Event.FetchAssessments -> {
                    fetchAssessments(event.student)
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

    sealed class Event{
        data class FetchAssessments(val student: Student) : Event()
    }

}