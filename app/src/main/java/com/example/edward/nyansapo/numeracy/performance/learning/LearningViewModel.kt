package com.example.edward.nyansapo.numeracy.performance.learning

import android.content.SharedPreferences
import android.service.autofill.Dataset

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.numeracy.Numeracy_Learning_Levels
import com.example.edward.nyansapo.presentation.ui.change_program.HomeRepository
import com.example.edward.nyansapo.presentation.ui.main.groupId
import com.example.edward.nyansapo.presentation.ui.main.programId
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.assessmentNumeracy
import com.example.edward.nyansapo.util.student
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LearningViewModel @ViewModelInject constructor(val repository: HomeRepository, val sharedPref: SharedPreferences) : ViewModel() {

    private val TAG = "LearningViewModel"


    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.StartFetchingCamps -> {
                    startFetchingCamps(sharedPref.programId!!, sharedPref.groupId!!)
                }
                is Event.FetchStudents -> {
                    fetchStudents(event.campId)
                }
                is Event.FetchCommonlyMissedProblems -> {
                    fetchCommonlyMissedProblems(event.campId)
                }
            }
        }
    }

    private suspend fun fetchCommonlyMissedProblems(campId: String) {
        repository.getStudents(campId).collect {
            Log.d(TAG, "fetchCommonlyMissedProblems: status:${it.status.name}")

            when (it.status) {
                Resource.Status.SUCCESS -> {
                    startAnalysis(it.data!!, campId)

                }
            }

        }

    }

    private suspend fun startAnalysis(students: List<DocumentSnapshot>, campId: String) {
        students.forEach {
            repository.fetchAssessmentsOnceSynchonous(campId, it.id).collect {
                fetchedAssessments(it.data!!)
            }
        }
    }

    val multiplication = com.example.edward.nyansapo.numeracy.DataSet.multiplication
    private fun fetchedAssessments(assessments: List<DocumentSnapshot>) {

        assessments.forEach {
            // it.assessmentNumeracy.wrongMultiplicationList.
        }

    }


    private val _fetchStudentsStatus = Channel<Resource<List<DocumentSnapshot>>>()
    val fetchStudentsStatus = _fetchStudentsStatus.receiveAsFlow()
    private suspend fun fetchStudents(campId: String) {
        repository.getStudents(campId).collect {
            Log.d(TAG, "fetchStudents: status:${it.status.name}")
            when (it.status) {

                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {
                    computeGraphData(it.data!!)

                }
                Resource.Status.ERROR -> {

                }
                Resource.Status.EMPTY -> {

                }
            }

        }

    }

    private val _computeGraphDataStatus = Channel<GraphData>()
    val computeGraphDataStatus = _computeGraphDataStatus.receiveAsFlow()
    private suspend fun computeGraphData(data: List<DocumentSnapshot>) {
        val beginnerCount = data.count { it.student.learningLevelNumeracy == Numeracy_Learning_Levels.BEGINNER.name }
        val additionCount = data.count { it.student.learningLevelNumeracy == Numeracy_Learning_Levels.ADDITION.name }
        val subtractionCount = data.count { it.student.learningLevelNumeracy == Numeracy_Learning_Levels.SUBTRACTION.name }
        val multiplicationCount = data.count { it.student.learningLevelNumeracy == Numeracy_Learning_Levels.MULTIPLICATION.name }
        val divisionCount = data.count { it.student.learningLevelNumeracy == Numeracy_Learning_Levels.DIVISION.name }
        val aboveCount = data.count { it.student.learningLevelNumeracy == Numeracy_Learning_Levels.ABOVE.name }

        val graphData = GraphData(data.size, beginnerCount, additionCount, subtractionCount, multiplicationCount, divisionCount, aboveCount)
        _computeGraphDataStatus.send(graphData)

    }


    //camp
    val campStatus = MutableStateFlow<Resource<QuerySnapshot>>(Resource.empty())
    suspend fun startFetchingCamps(programId: String, groupId: String) {
        Log.d(TAG, "startFetchingCamps: ")
        campStatus.value = Resource.loading("loading")

        try {
            repository.getCamps(programId, groupId).collect {

                campStatus.value = it
            }

        } catch (e: Exception) {

            Log.e("ERROR:", e.message)
        }

    }

    data class GraphData(val totalCount: Int, val beginnerCount: Int, val additionCount: Int, val subtractionCount: Int, val multiplicationCount: Int, val divisionCount: Int, val aboveCount: Int)
    sealed class Event {
        object StartFetchingCamps : Event()
        data class FetchStudents(val campId: String) : Event()
        data class FetchCommonlyMissedProblems(val campId: String) : Event()
    }
}