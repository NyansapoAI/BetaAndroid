package com.example.edward.nyansapo.assess_process.thank_you

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.numeracy.AssessmentNumeracy
import com.example.edward.nyansapo.numeracy.count_and_match.NumeracyRepository
import com.example.edward.nyansapo.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class ThankYouViewModel  @ViewModelInject constructor(private val repository: NumeracyRepository, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {


    private val TAG = "ThankYouViewModel"
    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {

                is Event.UpdateStudentLearningLevel_SaveAssessment -> {
                    updateSaveAssessement(event.assessmentNumeracy)
                }
            }


        }
    }

    private val _updateSaveStatus = Channel<Resource<AssessmentNumeracy>>()
    val updateSaveStatus = _updateSaveStatus.receiveAsFlow()
    private suspend fun updateSaveAssessement(assessmentNumeracy: AssessmentNumeracy) {
        repository.updateStudentLearningLevel(assessmentNumeracy).collect {
            Log.d(TAG, "updateSaveAssessement: updateStudentLearningLevel:${it.status.name}")
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    addAssessment(assessmentNumeracy)
                }
                Resource.Status.ERROR -> {
                    _updateSaveStatus.send(it)
                }

            }
        }

    }

    private suspend fun addAssessment(assessmentNumeracy: AssessmentNumeracy) {
       repository.addAssessment(assessmentNumeracy).collect {
           _updateSaveStatus.send(it)
       }
    }

    sealed class Event {
        data class UpdateStudentLearningLevel_SaveAssessment(val assessmentNumeracy: AssessmentNumeracy) : Event()
    }
}