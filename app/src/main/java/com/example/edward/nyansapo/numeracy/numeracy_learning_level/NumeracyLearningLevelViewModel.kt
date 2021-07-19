package com.example.edward.nyansapo.numeracy.numeracy_learning_level

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.numeracy.Numeracy_Learning_Levels
import com.example.edward.nyansapo.numeracy.count_and_match.NumeracyRepository
import com.example.edward.nyansapo.util.student
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
                is Event.StartQuery -> {
                    startQuery(event.query)
                }
            }
        }

    }

    private fun startQuery(query: String) {
        val students = allStudents.value.filter { it.student.firstname!!.contains(query, ignoreCase = true) || it.student.lastname!!.contains(query, ignoreCase = true) }
        unknownStudents.value = students.filter { it.student.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.UNKNOWN.name) }
        beginnerStudents.value = students.filter { it.student.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.BEGINNER.name) }
        additionStudents.value = students.filter { it.student.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.ADDITION.name) }
        subtractionStudents.value = students.filter { it.student.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.SUBTRACTION.name) }
        multiplicationStudents.value = students.filter { it.student.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.MULTIPLICATION.name) }
        divisionStudents.value = students.filter { it.student.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.DIVISION.name) }
        aboveStudents.value = students.filter { it.student.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.ABOVE.name) }

    }

    val unknownStudents = MutableStateFlow(listOf<DocumentSnapshot>())
    val beginnerStudents = MutableStateFlow(listOf<DocumentSnapshot>())
    val additionStudents = MutableStateFlow(listOf<DocumentSnapshot>())
    val subtractionStudents = MutableStateFlow(listOf<DocumentSnapshot>())
    val multiplicationStudents = MutableStateFlow(listOf<DocumentSnapshot>())
    val divisionStudents = MutableStateFlow(listOf<DocumentSnapshot>())
    val aboveStudents = MutableStateFlow(listOf<DocumentSnapshot>())
    private fun startSortingData(students: List<DocumentSnapshot>) {
        Log.d(TAG, "startSortingData:students size:${students.size} ")
        students.forEach {
            Log.d(TAG, "startSortingData: student:${it.student}")
        }
        unknownStudents.value = students.filter { it.student.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.UNKNOWN.name) }
        beginnerStudents.value = students.filter { it.student.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.BEGINNER.name) }
        additionStudents.value = students.filter { it.student.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.ADDITION.name) }
        subtractionStudents.value = students.filter { it.student.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.SUBTRACTION.name) }
        multiplicationStudents.value = students.filter { it.student.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.MULTIPLICATION.name) }
        divisionStudents.value = students.filter { it.student.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.DIVISION.name) }
        aboveStudents.value = students.filter { it.student.learningLevelNumeracy!!.equals(Numeracy_Learning_Levels.ABOVE.name) }
    }

    sealed class Event {
        data class StartSortingData(val students: List<DocumentSnapshot>) : Event()
        data class StartQuery(val query: String) : Event()
    }


}