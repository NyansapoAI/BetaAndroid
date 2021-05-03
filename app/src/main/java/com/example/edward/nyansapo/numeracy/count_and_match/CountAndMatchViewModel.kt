package com.example.edward.nyansapo.numeracy.count_and_match

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel

class CountAndMatchViewModel @ViewModelInject constructor(private val repository: NumeracyRepository) : ViewModel() {
    var counter = 0
    val getCountAndMatch = repository.countAndMatch
    var correctCount = 0
    fun getBalls(): Int {
        return getCountAndMatch.value.data!![counter]
    }
}