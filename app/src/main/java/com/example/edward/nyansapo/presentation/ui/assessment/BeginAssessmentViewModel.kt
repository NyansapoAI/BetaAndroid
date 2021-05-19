package com.example.edward.nyansapo.presentation.ui.assessment

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.STUDENT_ARG
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.util.Resource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class BeginAssessmentViewModel @ViewModelInject constructor(private val repo: BeginAssessmentRepo, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {


    val getStudent = repo.getStudent(savedStateHandle.get<Student>(STUDENT_ARG)!!.id!!)
    private val _beginAssessmentEvents = Channel<BeginAssessmentFragment.Event>()
    val beginAssessmentEvents = _beginAssessmentEvents.receiveAsFlow()
    fun setEvent(event: BeginAssessmentFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is BeginAssessmentFragment.Event.BeginAssessmentClicked -> {
                    _beginAssessmentEvents.send(BeginAssessmentFragment.Event.BeginAssessmentClicked(event.student))
                }
                is BeginAssessmentFragment.Event.GetAssessments -> {
                    getAssessments(event.snapshot)
                }

            }
        }
    }

    private val _getAssessmentsStatus = Channel<Resource<List<DocumentSnapshot>>>()
    val getAssessmentsStatus = _getAssessmentsStatus.receiveAsFlow()
    private suspend fun getAssessments(snapshot: DocumentSnapshot) {
        repo.getAssessments(snapshot).collect {
            _getAssessmentsStatus.send(it)
        }
    }
}