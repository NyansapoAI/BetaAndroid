package com.example.edward.nyansapo

import android.os.Parcelable
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.edward.nyansapo.R
import com.example.edward.nyansapo.numeracy.Numeracy_Learning_Levels
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize
import java.util.*


const val TIME_STAMP_FIELD = "timestamp"
const val STUDENT_ARG = "student"

@Parcelize
data class Student(
        @DocumentId
        val id: String? = null,
        var firstname: String? = null,
        var lastname: String? = null,
        var age: String? = null,
        var gender: String? = null,
        var location: String? = null,

        @ServerTimestamp
        var timestamp: Date? = null,
        var instructor_id: String? = null,
        var learningLevel: String? = Learning_Level.UNKNOWN.name,
        var learningLevelNumeracy: String? = Numeracy_Learning_Levels.UNKNOWN.name,
        var std_class: String? = null,
        var avatar: Int = R.drawable.nyansapo_avatar_lion

) : Parcelable, SearchSuggestion {

    override fun getBody(): String {
        return "$firstname $lastname"
    }

    override fun toString(): String {
        return "Names: $firstname $lastname :Id:$id:LearningLevel:$learningLevel:learningLevelNumeracy:$learningLevelNumeracy"

    }
}