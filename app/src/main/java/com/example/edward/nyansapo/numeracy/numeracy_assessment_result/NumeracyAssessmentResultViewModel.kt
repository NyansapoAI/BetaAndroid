package com.example.edward.nyansapo.numeracy.numeracy_assessment_result

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.numeracy.AssessmentNumeracy
import com.example.edward.nyansapo.numeracy.Numeracy_Learning_Levels
import com.example.edward.nyansapo.numeracy.count_and_match.NumeracyRepository
import com.example.edward.nyansapo.presentation.ui.preassessment.MainRepository
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.assessmentNumeracy
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class NumeracyAssessmentResultViewModel @ViewModelInject constructor(private val repository: MainRepository) : ViewModel() {

    var counter = 0
    private val _numeracyAssessmentResultEvents = Channel<Event>()
    val numeracyAssessmentResultEvents = _numeracyAssessmentResultEvents.receiveAsFlow()
    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.FetchAssessments -> {
                    fetchAssessments(event.student)
                }
                is Event.BackClicked -> {
                    backClicked()
                }
                is Event.FrontClicked -> {
                    frontClicked()
                }
                is Event.FirstScreen -> {
                    firstScreen(event.choosen)
                }

            }
        }
    }

    private suspend fun firstScreen(choosen: Int) {
        counter = choosen
        display(assessMentsListFlow.value.get(counter))
    }

    private suspend fun frontClicked() {
        if (counter + 1 >= assessMentsListFlow.value.size) {
            return
        } else {
            counter++
            display(assessMentsListFlow.value.get(counter))

        }
    }

    private suspend fun backClicked() {
        if (counter - 1 < 0) {
            return
        } else {

            counter--
            display(assessMentsListFlow.value.get(counter))

        }


    }

    private suspend fun display(snapshot: DocumentSnapshot) {
        val assessment = snapshot.assessmentNumeracy

        _numeracyAssessmentResultEvents.send(Event.DisplayAssessment(assessment))

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

    sealed class Event {
        data class FetchAssessments(val student: Student) : Event()
        data class Beginner(val assessment: AssessmentNumeracy) : Event()
        data class Addition(val assessment: AssessmentNumeracy) : Event()
        data class Subtraction(val assessment: AssessmentNumeracy) : Event()
        data class Multiplicaton(val assessment: AssessmentNumeracy) : Event()
        data class DisplayAssessment(val assessment: AssessmentNumeracy) : Event()
        data class Division(val assessment: AssessmentNumeracy) : Event()
        data class FirstScreen(val choosen: Int) : Event()
        object BackClicked : Event()
        object FrontClicked : Event()
    }
}