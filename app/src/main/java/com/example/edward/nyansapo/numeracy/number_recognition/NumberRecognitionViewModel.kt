package com.example.edward.nyansapo.numeracy.number_recognition

import android.content.Context
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edward.nyansapo.R
import com.example.edward.nyansapo.numeracy.count_and_match.NumeracyRepository
import com.example.edward.nyansapo.wrappers.Resource
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

    var counter = 0
    val getNumberRecogn_2 = repository.numberRecognition_2
    var correctCount = 0
    fun getCurrentNumber(): Int {
        return getNumberRecogn_2.value.data!![counter]
    }

    private val _numberRecognitionEvents = Channel<NumberRecognition2Fragment.Event>()
    val numberRecognitionEvents = _numberRecognitionEvents.receiveAsFlow()

    private val _recognitionStatus = Channel<Resource<String>>()
    val recognitionStatus = _recognitionStatus.receiveAsFlow()
    fun setEvent(event: NumberRecognition2Fragment.Event) {
        viewModelScope.launch {
            when (event) {
                is NumberRecognition2Fragment.Event.RecordStudent -> {
                    recordStudent()
                }
                is NumberRecognition2Fragment.Event.CheckIfCorrect -> {
                    checkIfCorrect(event.recorded)
                }

            }
        }
    }

    private suspend fun checkIfCorrect(recorded: String) {
        Log.d(TAG, "checkIfCorrect: recorded:$recorded")
        val cleanResult = recorded.cleanResult
        Log.d(TAG, "checkIfCorrect: cleanResult:$cleanResult")
        Log.d(TAG, "checkIfCorrect: getNumberRecogn_2:${getCurrentNumber()}::cleanResult :$cleanResult")
        if (getCurrentNumber().toString() == cleanResult) {
            Log.d(TAG, "checkIfCorrect: correct")
            correctCount++
        } else {
            Log.d(TAG, "checkIfCorrect: wrong")
        }
        counter++
        if (counter < getNumberRecogn_2.value.data!!.size) {
            _numberRecognitionEvents.send(NumberRecognition2Fragment.Event.Next)
        } else {
            _numberRecognitionEvents.send(NumberRecognition2Fragment.Event.Finished)

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

}