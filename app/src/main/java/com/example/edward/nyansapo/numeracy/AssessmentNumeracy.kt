package com.example.edward.nyansapo.numeracy

import android.os.Parcelable
import com.example.edward.nyansapo.Student
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AssessmentNumeracy(
        val student: Student = Student(),
        val id: String = "",
        val correctCountAndMatch: Int = 0,
        val correctNumberRecognition: Int = 0,
        val correctAddition: Int = 0,
        val correctSubtraction: Int = 0,
        val correctMultiplication: Int = 0,
        val correctDivision: Int = 0,
        val wordProblemIsCorrect: Boolean = false,
        var learningLevel: String = "UNKNOWN",
):Parcelable