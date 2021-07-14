package com.example.edward.nyansapo.util

import androidx.appcompat.widget.SearchView
import com.example.edward.nyansapo.Assessment
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.numeracy.AssessmentNumeracy
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.*

inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })
}


val <T> T.exhaustive: T
    get() = this

val String.cleanTranscriptionTxt
    get() = this.toLowerCase().replace(".", "")!!.replace(",", "")

val String.sentenceToList: List<String>
    get() = this.split(" ").map {
        it.trim()
    }.filter {
        it.isNotBlank()
    }

val Date.formatDate get() = SimpleDateFormat("dd/MM/yyyy").format(this)
val String.cleanString
    get() =
        this.replace("/", "_")


val DocumentSnapshot.student get() = this.toObject(Student::class.java)!!
val DocumentSnapshot.assessment get() = this.toObject(Assessment::class.java)!!
val DocumentSnapshot.assessmentNumeracy get() = this.toObject(AssessmentNumeracy::class.java)!!