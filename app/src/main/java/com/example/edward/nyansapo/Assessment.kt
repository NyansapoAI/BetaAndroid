package com.example.edward.nyansapo

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Assessment(
        var id: String,
        var assessmentKey: String,
        var letterCorrect: String,
        var lettersWrong: String = "",
        var wordsCorrect: String = "",
        var wordsWrong: String = "",
        var paragraphWordsWrong: String = "",
        var paragraphChoosen: Int =0,
        var storyWordsWrong: String = "",

        var storyAnswerQ1: String = "",
        var storyAnswerQ2: String = "",
        var learningLevel: String = "UNKNOWN",
        @ServerTimestamp
        val timestamp: Date? = null,
        var LOCAL_ID: String = "",
        var CLOUD_ID: String = ""
) : Parcelable {
    constructor() : this("", "", "", "", "", "", "", 0,"", "", "", "", null, "", "")
}