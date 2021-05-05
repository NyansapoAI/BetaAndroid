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

    var counter = 0
    val getCountAndMatch = repository.countAndMatch
    var correctCount = 0
    fun getCurrent(): Int {
        return getCountAndMatch.value.data!![counter]
    }

    private val _countAndMatchEvents = Channel<CountAndMatchFragment.Event>()
    val countAndMatchEvents = _countAndMatchEvents.receiveAsFlow()
    fun setEvent(event: CountAndMatchFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is CountAndMatchFragment.Event.ChoiceClicked -> {
                    choiceClicked(event.choice)
                }
                is CountAndMatchFragment.Event.BallClicked -> {
                    ballClicked()
                }
                is CountAndMatchFragment.Event.Refresh -> {
                    refresh()
                }

            }
        }
    }

    private suspend fun refresh() {
        numberOfSelectedBalls = 0
        _countAndMatchEvents.send(CountAndMatchFragment.Event.Refresh)
    }

    private suspend fun ballClicked() {
        numberOfSelectedBalls++
        if (numberOfSelectedBalls == getCurrent()) {
            _countAndMatchEvents.send(CountAndMatchFragment.Event.EnableChoices)
        }
    }

    private suspend fun choiceClicked(choice: Int) {
        if (choice == getCurrent()) {
            correctCount++
            Log.d(TAG, "choiceClicked: answer correct")
        } else {
            Log.d(TAG, "choiceClicked: answer wrong")
        }
        //increment counter
        counter++
        if (counter < getCountAndMatch.value.data!!.size) {
            _countAndMatchEvents.send(CountAndMatchFragment.Event.Next)
        } else {
            _countAndMatchEvents.send(CountAndMatchFragment.Event.Finished)
        }
    }

    var numberOfSelectedBalls = 0
}