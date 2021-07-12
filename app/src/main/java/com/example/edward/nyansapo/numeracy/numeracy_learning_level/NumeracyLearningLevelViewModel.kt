package com.example.edward.nyansapo.numeracy.numeracy_learning_level

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.numeracy.Numeracy_Learning_Levels
import com.example.edward.nyansapo.numeracy.count_and_match.NumeracyRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NumeracyLearningLevelViewModel @ViewModelInject constructor(private val repository: NumeracyRepository) : ViewModel() {


    private val TAG = "NumeracyLearningLevelVi"

    val getAllStudents = repository.getAllStudents()

    val allStudents = MutableStateFlow(listOf<DocumentSnapshot>())
    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.StartSortingData -> {
                    allStudents.value = event.students
                    startSortingData(event.students)
                }
            }
        }

    }

    val beginnerStudents = MutableStateFlow(listOf<DocumentSnapshot>())
    val additionStudents = MutableStateFlow(listOf<DocumentSnapshot>())
    private fun startSortingData(students: List<DocumentSnapshot>) {
        Log.d(TAG, "startSortingData:students size:${students.size} ")
        beginnerStudents.value = students.filter { it.toObject(Student::class.java)!!.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.BEGINNER.name) }
        additionStudents.value = students.filter { it.toObject(Student::class.java)!!.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.ADDITION.name) }
    }

    sealed class Event {
        data class StartSortingData(val students: List<DocumentSnapshot>) : Event()
    }


}