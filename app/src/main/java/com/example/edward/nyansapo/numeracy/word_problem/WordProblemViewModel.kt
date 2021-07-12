package com.example.edward.nyansapo.numeracy.word_problem

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.numeracy.count_and_match.NumeracyRepository
import com.example.edward.nyansapo.util.Resource
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.digitalink.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class WordProblemViewModel @ViewModelInject constructor(private val repository: NumeracyRepository) : ViewModel() {

    private val TAG = "WordProblemViewModel"

    val getWordProblem = repository.wordProblem

    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.StartAnalysis -> {
                    startAnalysis(event.inkBuilder)
                }

            }
        }
    }

    private val _analysesStatus = Channel<Resource<Boolean>>()
    val analysesStatus = _analysesStatus.receiveAsFlow()

    private suspend fun startAnalysis(inkBuilder: Ink.Builder) {
        Log.d(TAG, "startAnalysis: ")
        val recognizer = getDigitalInkRecognizer()
        _analysesStatus.send(Resource.loading("Analysing..."))
        recognizer.recognize(inkBuilder.build())
                .addOnSuccessListener { result: RecognitionResult ->
                    // `result` contains the recognizer's answers as a RecognitionResult.
                    // Logs the text from the top candidate.

                    Log.d(TAG, "startAnalysis:Best Result:: ${result.candidates[0].text}")

                    result.candidates.forEachIndexed { index, recognitionCandidate ->
                        Log.d(TAG, "startAnalysis: index:$index Result:${result.candidates[index].text}")

                    }
                    viewModelScope.launch {
                         answerReceived(result.candidates)
                    }

                }
                .addOnFailureListener { e: Exception ->
                    viewModelScope.launch { _analysesStatus.send(Resource.error(e)) }

                    Log.d(TAG, "startAnalysis: Error:${e.message}")
                    Log.e(TAG, "Error during recognition: $e")
                    e.printStackTrace()
                }

    }

    private suspend fun answerReceived(writtenAnswer: List<RecognitionCandidate>) {

        val correctAnswer = getWordProblem.value.data!!.second
        if (answerIsCorrect(writtenAnswer, correctAnswer)) {
            Log.d(TAG, "answerReceived: correct")
            _analysesStatus.send(Resource.success(true))
        } else {
            _analysesStatus.send(Resource.success(false))
            Log.d(TAG, "answerReceived: wrong")

        }
    }

    private fun answerIsCorrect(writtenAnswer: List<RecognitionCandidate>, correctAnswer: String): Boolean {
        writtenAnswer.forEachIndexed { index, recognitionCandidate ->
            if (recognitionCandidate.text.trim().equals(correctAnswer)) {
                Log.d(TAG, "answerReceived:${recognitionCandidate.text} :correctAnswer:$correctAnswer")
                return true
            }
        }
        Log.d(TAG, "answerReceived:${writtenAnswer[0].text} :correctAnswer:$correctAnswer")

        return false
    }

    private fun getDigitalInkRecognizer(): DigitalInkRecognizer {
        Log.d(TAG, "getDigitalInkRecognizer: ")
        // Specify the recognition model for a language
        var modelIdentifier: DigitalInkRecognitionModelIdentifier? = null
        try {
            modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US")
        } catch (e: MlKitException) {
            e.printStackTrace()
            // language tag failed to parse, handle error.
        }
        if (modelIdentifier == null) {
            Log.d(TAG, "getDigitalInkRecognizer: modelIdentifier is null")
            // no model was found, handle error.
        }
        var model: DigitalInkRecognitionModel =
                DigitalInkRecognitionModel.builder(modelIdentifier).build()


// Get a recognizer for the language
        var recognizer: DigitalInkRecognizer =
                DigitalInkRecognition.getClient(
                        DigitalInkRecognizerOptions.builder(model).build())

        return recognizer
    }

    sealed class Event {
        data class StartAnalysis(val inkBuilder: Ink.Builder) : Event()

    }

}