package com.example.edward.nyansapo.numeracy.count_and_match

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CountAndMatchViewModel @ViewModelInject constructor(private val repository: NumeracyRepository) : ViewModel() {

    private val TAG = "CountAndMatchViewModel"

    private val numberToPass = 4
    var counter = 0
    val getCountAndMatch = repository.countAndMatch
    var correctCount = 0
    fun getCurrent(): Int {
        return getCountAndMatch.value.data!![counter]
    }

    private val _countAndMatchEvents = Channel<Event>()
    val countAndMatchEvents = _countAndMatchEvents.receiveAsFlow()
    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.ChoiceClicked -> {
                    choiceClicked(event.choice)
                }
                is Event.BallClicked -> {
                    ballClicked()
                }
                is Event.Refresh -> {
                    refresh()
                }

            }
        }
    }

    private suspend fun refresh() {
        numberOfSelectedBalls = 0
        _countAndMatchEvents.send(Event.Refresh)
    }

    private suspend fun ballClicked() {
        numberOfSelectedBalls++
        if (numberOfSelectedBalls == getCurrent()) {
            _countAndMatchEvents.send(Event.EnableChoices)
        }
    }

    private val correctList: MutableList<Int> = mutableListOf()
    private val wrongList: MutableList<Int> = mutableListOf()
    private suspend fun choiceClicked(choice: Int) {
        if (choice == getCurrent()) {
            correctCount++
            Log.d(TAG, "choiceClicked: answer correct")
            correctList.add(getCurrent())
        } else {
            Log.d(TAG, "choiceClicked: answer wrong")
            wrongList.add(getCurrent())
        }
        //increment counter
        counter++
        if (counter < getCountAndMatch.value.data!!.size) {
            _countAndMatchEvents.send(Event.Next)
        } else {

            if (correctCount >= numberToPass) {
                _countAndMatchEvents.send(Event.FinishedPassed(correctList, wrongList))
            } else {
                _countAndMatchEvents.send(Event.FinishedFailed(correctList, wrongList))
            }

        }
    }

    var numberOfSelectedBalls = 0

    sealed class Event {
        data class ChoiceClicked(val choice: Int) : Event()
        object Next : Event()
        data class FinishedPassed(val correctList: MutableList<Int>, val wrongList: MutableList<Int>) : Event()
        data class FinishedFailed(val correctList: MutableList<Int>, val wrongList: MutableList<Int>) : Event()
        object BallClicked : Event()
        object EnableChoices : Event()
        object Refresh : Event()
    }
}