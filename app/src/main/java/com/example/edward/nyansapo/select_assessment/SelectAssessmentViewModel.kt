package com.example.edward.nyansapo.select_assessment

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edward.nyansapo.R
import com.example.edward.nyansapo.STUDENT_ARG
import com.example.edward.nyansapo.Student
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SelectAssessmentViewModel @ViewModelInject constructor(@Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _selectAssessmentEvents = Channel<SelectAssessmentFragment.Event>()
    val selectAssessmentEvents = _selectAssessmentEvents.receiveAsFlow()

    fun setEvent(event: SelectAssessmentFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is SelectAssessmentFragment.Event.AssessmentClicked -> {
                    assessmentClicked(event.viewId)
                }
            }
        }
    }

    private suspend fun assessmentClicked(viewId: Int?) {
        val student = savedStateHandle.get<Student>(STUDENT_ARG)!!
        val event: SelectAssessmentFragment.Event.GoToPreAssessment?
        when (viewId) {
            R.id.assessment3_button -> {
                event = SelectAssessmentFragment.Event.GoToPreAssessment(3, student)
            }
            R.id.assessment4_button -> {
                event = SelectAssessmentFragment.Event.GoToPreAssessment(4, student)
            }
            R.id.assessment5_button -> {
                event = SelectAssessmentFragment.Event.GoToPreAssessment(5, student)
            }
            R.id.assessment6_button -> {
                event = SelectAssessmentFragment.Event.GoToPreAssessment(6, student)
            }
            R.id.assessment7_button -> {
                event = SelectAssessmentFragment.Event.GoToPreAssessment(7, student)
            }
            R.id.assessment8_button -> {
                event = SelectAssessmentFragment.Event.GoToPreAssessment(8, student)
            }
            R.id.assessment9_button -> {
                event = SelectAssessmentFragment.Event.GoToPreAssessment(9, student)
            }
            else -> {
                event = SelectAssessmentFragment.Event.GoToPreAssessment(10, student)
            }

        }

        _selectAssessmentEvents.send(event)

    }

}