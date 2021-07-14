package com.example.edward.nyansapo.numeracy

import android.os.Parcelable
import com.example.edward.nyansapo.Student
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AssessmentNumeracy(
        val student: Student = Student(),
        val id: String = "",
        val correctCountAndMatch: Int = 0,
        val correctCountAndMatchList: MutableList<Int> = mutableListOf(),
        val wrongCountAndMatchList: MutableList<Int> = mutableListOf(),
        val correctNumberRecognition: Int = 0,
        val correctNumberRecognitionList: MutableList<Int> = mutableListOf(),
        val wrongNumberRecognitionList: MutableList<Int> = mutableListOf(),

        val correctAddition: Int = 0,
        val correctSubtraction: Int = 0,
        val correctMultiplication: Int = 0,
        val correctDivision: Int = 0,
        val correctAdditionList: MutableList<Problem> = mutableListOf(),
        val wrongAdditionList: MutableList<Problem> = mutableListOf(),
        val correctSubtractionList: MutableList<Problem> = mutableListOf(),
        val wrongSubtractionList: MutableList<Problem> = mutableListOf(),
        val correctMultiplicationList: MutableList<Problem> = mutableListOf(),
        val wrongMultiplicationList: MutableList<Problem> = mutableListOf(),
        val correctDivisionList: MutableList<Problem> = mutableListOf(),
        val wrongDivisionList: MutableList<Problem> = mutableListOf(),
        val wordProblemIsCorrect: Boolean = false,
        val correctWordProblemAnswer: Int = 0,
        val wrongWordProblemAnswer: String = "",
        var learningLevelNumeracy: String = Numeracy_Learning_Levels.UNKNOWN.name,
) : Parcelable