package com.example.edward.nyansapo.numeracy.number_recognition

import android.content.Context
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edward.nyansapo.R
import com.example.edward.nyansapo.numeracy.count_and_match.CountAndMatchViewModel
import com.example.edward.nyansapo.numeracy.count_and_match.NumeracyRepository
import com.example.edward.nyansapo.util.Resource
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutionException


class NumberRecognitionViewModel @ViewModelInject constructor(private val repository: NumeracyRepository, @ApplicationContext private val context: Context) : ViewModel() {

    private val TAG = "NumberRecognitionViewMo"
    private val numberToPass = 4

    var counter = 0
    val getNumberRecogn_2 = repository.numberRecognition_2
    var correctCount = 0
    fun getCurrentNumber(): Int {
        return getNumberRecogn_2.value.data!![counter]
    }

    private val _numberRecognitionEvents = Channel<Event>()
    val numberRecognitionEvents = _numberRecognitionEvents.receiveAsFlow()

    private val _recognitionStatus = Channel<Resource<String>>()
    val recognitionStatus = _recognitionStatus.receiveAsFlow()
    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.RecordStudent -> {
                    recordStudent()
                }
                is Event.CheckIfCorrect -> {
                    checkIfCorrect(event.recorded)
                }

            }
        }
    }

    private val correctList: MutableList<Int> = mutableListOf()
    private val wrongList: MutableList<Int> = mutableListOf()

    private suspend fun checkIfCorrect(recorded: String) {
        Log.d(TAG, "checkIfCorrect: recorded:$recorded")
        val cleanResult = recorded.cleanResult
        Log.d(TAG, "checkIfCorrect: cleanResult:$cleanResult")
        Log.d(TAG, "checkIfCorrect: getNumberRecogn_2:${getCurrentNumber()}::cleanResult :$cleanResult")
        if (getCurrentNumber().toString() == cleanResult) {
            Log.d(TAG, "checkIfCorrect: correct")
            correctCount++
            correctList.add(getCurrentNumber())
        } else {
            Log.d(TAG, "checkIfCorrect: wrong")
            wrongList.add(getCurrentNumber())

        }
        counter++
        if (counter < getNumberRecogn_2.value.data!!.size) {
            _numberRecognitionEvents.send(Event.Next)
        } else {
            if (correctCount >= numberToPass) {
                _numberRecognitionEvents.send(Event.FinishedPassed(correctList, wrongList))
            } else {
                _numberRecognitionEvents.send(Event.FinishedFailed(correctList, wrongList))
            }
        }

    }

    private fun recordStudent() {
        viewModelScope.launch(Dispatchers.IO) {
            _recognitionStatus.send(Resource.loading(""))
            startRecording()
        }
    }

    private suspend fun startRecording() {
        Log.d(TAG, "startRecording: ")
        val speechSubscriptionKey = context.getString(R.string.speech_subscription_key)
        val serviceRegion = context.getString(R.string.service_region)
        val config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion)

        config!!.endpointId = context.getString(R.string.end_point)
        val reco = SpeechRecognizer(config)
        var result: SpeechRecognitionResult? = null
        val task = reco!!.recognizeOnceAsync()!!


        try {
            result = task.get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        if (result!!.reason == ResultReason.RecognizedSpeech) {

            _recognitionStatus.send(Resource.success(result.text))

        } else if (result.reason == ResultReason.NoMatch) {
            _recognitionStatus.send(Resource.error(Exception("No Match")))
        } else if (result.reason == ResultReason.Canceled) {
            _recognitionStatus.send(Resource.error(Exception("Cancelled")))


        }

    }

    sealed class Event {
        object RecordStudent : Event()
        data class CheckIfCorrect(val recorded: String) : Event()
        object Next : Event()
        data class FinishedPassed(val correctList: MutableList<Int>, val wrongList: MutableList<Int>) : Event()
        data class FinishedFailed(val correctList: MutableList<Int>, val wrongList: MutableList<Int>) : Event()

    }

}