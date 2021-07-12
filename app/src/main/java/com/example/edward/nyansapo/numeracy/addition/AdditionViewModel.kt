package com.example.edward.nyansapo.numeracy.addition

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.numeracy.addition.AdditionViewModel_2.*
import com.example.edward.nyansapo.numeracy.count_and_match.NumeracyRepository
import com.example.edward.nyansapo.util.Resource
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdditionViewModel @ViewModelInject constructor(private val repository: NumeracyRepository) : ViewModel() {

    private val TAG = "AdditionViewModel"

    var counter = 0
    val getAddition = repository.getAddition
    var correctCount = 0
    fun getCurrentNumber(): Pair<Int, Int> {
        return getAddition.value.data!![counter]
    }

    private val _additionEvents = Channel<Event>()
    val additionEvents = _additionEvents.receiveAsFlow()

    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.StartModelDownload -> {
                    startModelDownload()
                }
                is Event.StartAnalysis -> {
                    startAnalysis(event.inkBuilder)
                }

            }
        }
    }

    private val _analysesStatus = Channel<Resource<String>>()
    val analysesStatus = _analysesStatus.receiveAsFlow()

    private suspend fun startAnalysis(inkBuilder: Ink.Builder) {
        Log.d(TAG, "startAnalysis: ")
        if (!modelIsPresent) {
            _analysesStatus.send(Resource.error(java.lang.Exception("Model is Absent")))
            return
        }


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
                        _analysesStatus.send(Resource.success(result.candidates[0].text))
                        answerReceived(result.candidates[0].text)
                    }

                }
                .addOnFailureListener { e: Exception ->
                    viewModelScope.launch { _analysesStatus.send(Resource.error(e)) }

                    Log.d(TAG, "startAnalysis: Error:${e.message}")
                    Log.e(TAG, "Error during recognition: $e")
                    e.printStackTrace()
                }

    }

    private suspend fun answerReceived(writtenAnswer: String) {
        Log.d(TAG, "answerReceived: writtenAnswer:$writtenAnswer")

        val current = getCurrentNumber()
        val answer = current.first + current.second
        if (writtenAnswer == answer.toString()) {
            Log.d(TAG, "answerReceived: correct")
            correctCount++
        } else {
            Log.d(TAG, "answerReceived: wrong")
        }
        counter++
        if (counter < getAddition.value.data!!.size) {
            _additionEvents.send(Event.Next)
        } else {
            _additionEvents.send(Event.Finished)

        }
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

    private suspend fun startModelDownload() {
        downloadModel()
    }

    private suspend fun downloadModel() {
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

        val remoteModelManager = RemoteModelManager.getInstance()
        _modelDownloadStatus.send(Resource.loading("Downloading Model.."))
        remoteModelManager.download(model, DownloadConditions.Builder().build())
                .addOnSuccessListener {
                    Log.i(TAG, "Model downloaded")
                    modelIsPresent = true
                    viewModelScope.launch { _modelDownloadStatus.send(Resource.success(true)) }
                }
                .addOnFailureListener { e: Exception ->
                    viewModelScope.launch { _modelDownloadStatus.send(Resource.error(e)) }
                    Log.e(TAG, "Error while downloading a model: $e")
                    e.printStackTrace()

                }
    }

    var modelIsPresent = false
    private val _modelDownloadStatus = Channel<Resource<Boolean>>()
    val modelDownloadStatus = _modelDownloadStatus.receiveAsFlow()
    private val _modelPresentStatus = Channel<Resource<Boolean>>()
    val modelPresentStatus = callbackFlow<Resource<Boolean>> {

        Log.d(TAG, "checkIfModelIsDownloaded: ")
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
        val remoteModelManager = RemoteModelManager.getInstance()

        send(Resource.loading("Checking If Model Exists"))
        modelIsPresent = remoteModelManager.isModelDownloaded(model).await()
        if (modelIsPresent) {
            send(Resource.success(true))
        } else {
            send(Resource.error(Exception("Model Is Absent")))
        }
        awaitClose { }

    }


}