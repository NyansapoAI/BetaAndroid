package com.example.edward.nyansapo.numeracy.number_recognition

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.edward.nyansapo.numeracy.count_and_match.NumeracyRepository

class NumberRecognitionViewModel @ViewModelInject constructor(private val repository: NumeracyRepository) : ViewModel() {

    var counter = 0
    val getNumberRecogn_2 = repository.numberRecognition_2
    var correctCount = 0
    fun getCurrentNumber(): Int {
        return getNumberRecogn_2.value.data!![counter]
    }
}