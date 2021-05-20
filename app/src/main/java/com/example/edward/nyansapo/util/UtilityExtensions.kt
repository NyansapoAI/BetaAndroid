package com.example.edward.nyansapo.util

import androidx.appcompat.widget.SearchView

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