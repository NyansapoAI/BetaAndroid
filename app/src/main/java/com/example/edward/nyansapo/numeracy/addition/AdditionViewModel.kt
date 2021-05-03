package com.example.edward.nyansapo.numeracy.addition

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.edward.nyansapo.numeracy.count_and_match.NumeracyRepository

class AdditionViewModel @ViewModelInject constructor(private val repository: NumeracyRepository) : ViewModel() {

    var counter = 0
    val getAddition = repository.getAddition
    var correctCount = 0
    fun getCurrentNumber(): Pair<Int,Int> {
        return getAddition.value.data!![counter]
    }
}