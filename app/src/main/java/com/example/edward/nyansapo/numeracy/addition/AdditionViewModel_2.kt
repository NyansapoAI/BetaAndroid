package com.example.edward.nyansapo.numeracy.addition

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.numeracy.Operators
import com.example.edward.nyansapo.numeracy.count_and_match.NumeracyRepository
import com.example.edward.nyansapo.util.Constants
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.exhaustive
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdditionViewModel_2 @ViewModelInject constructor(private val repository: NumeracyRepository) : ViewModel() {

    private val TAG = "AdditionViewModel2"
    lateinit var operator: Operators
    var counter = 0
    lateinit var getData: MutableStateFlow<Resource<Array<Pair<Int, Int>>>>
    var correctCount = 0
    fun getCurrentNumber(): Pair<Int, Int> {
        return getData.value.data!![counter]
    }


    fun setOperation(operators: Operators) {
        operator = operators
        getData = getData()
    }

    private fun getData() =
            when (operator) {
                Operators.ADDITION -> repository.getAddition
                Operators.SUBTRACTION -> repository.getSubtraction
                Operators.MULTIPLICATION -> repository.getMultiplication
                Operators.DIVISION -> repository.getDivision
            }.exhaustive


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

    private suspend fun answerReceived(writtenAnswers: List<RecognitionCandidate>) {
        val correctAnswer = getCorrectAnswer()
        Log.d(TAG, "answerReceived: :correctAnswer:$correctAnswer")

        if (answerIsCorrect(writtenAnswers, correctAnswer)) {
            Log.d(TAG, "answerReceived: correct")
            correctCount++
        } else {
            Log.d(TAG, "answerReceived: wrong")
        }
        counter++
        if (counter < getData.value.data!!.size) {
            _additionEvents.send(Event.Next)
        } else {
            _additionEvents.send(Event.Finished)

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

    private fun getCorrectAnswer(): String {
        val current = getCurrentNumber()
        var answer: Int = 0
        when (operator) {
            Operators.ADDITION -> {
                answer = current.first + current.second
            }
            Operators.SUBTRACTION -> {
                answer = current.first - current.second
            }
            Operators.MULTIPLICATION -> {
                answer = current.first * current.second
            }
            Operators.DIVISION -> {
                answer = current.first / current.second
            }
        }

        return answer.toString()

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

    var modelIsPresent = Constants.modelIsPresent
    private val _modelDownloadStatus = Channel<Resource<Boolean>>()
    val modelDownloadStatus = _modelDownloadStatus.receiveAsFlow()
    private val _modelPresentStatus = Channel<Resource<Boolean>>()
    val modelPresentStatus = callbackFlow<Resource<Boolean>> {
        if (Constants.modelIsPresent) {
            awaitClose { }

            return@callbackFlow
        }

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
        Constants.modelIsPresent = modelIsPresent
        if (modelIsPresent) {
            send(Resource.success(true))
        } else {
            send(Resource.error(Exception("Model Is Absent")))
        }
        awaitClose { }

    }

    sealed class Event {
        object RecordStudent : Event()
        data class CheckIfCorrect(val recorded: String) : Event()
        object CheckIfModelIsDownloaded : Event()
        object StartModelDownload : Event()
        data class StartAnalysis(val inkBuilder: Ink.Builder) : Event()
        object Next : Event()
        object Finished : Event()
    }
}