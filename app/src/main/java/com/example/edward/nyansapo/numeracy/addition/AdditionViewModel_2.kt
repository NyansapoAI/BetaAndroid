package com.example.edward.nyansapo.numeracy.addition

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.numeracy.Operators
import com.example.edward.nyansapo.numeracy.Problem
import com.example.edward.nyansapo.numeracy.count_and_match.NumeracyRepository
import com.example.edward.nyansapo.util.Constants
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.exhaustive
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.*
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVision
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager
import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionImpl
import com.microsoft.azure.cognitiveservices.vision.computervision.models.OperationStatusCodes
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadOperationResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class AdditionViewModel_2 @ViewModelInject constructor(private val repository: NumeracyRepository) : ViewModel() {
    private val numberOfCorrect = 2
    private val correctList = mutableListOf<Problem>()
    private val wrongList = mutableListOf<Problem>()
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
                    startAnalysis(event.inkBuilder, event.width, event.height)
                }
                is Event.StartAnalysisAzure -> {
                    startAnalysisAzure(event.path)
                }

            }
        }
    }

    ///////////////azure
    private fun startAnalysisAzure(path: String) {
        Log.d(TAG, "startAnalysisAzure: path:$path")
        val subscriptionKey = "180cd0c2c86743ffbcd2560096b2956c"
        val endpoint = "https://nyansapovision.cognitiveservices.azure.com/"
        // Create an authenticated Computer Vision client.
        val compVisClient = Authenticate(subscriptionKey, endpoint)

        val rawImage = File(path)
        //  val localImageBytes: ByteArray = Files.readAllBytes(rawImage.toPath())
        // Read from local file
        //  ReadFromFile(compVisClient!!, localImageBytes)
        val excutor = Executors.newSingleThreadExecutor().execute {
            ReadFromUrl(compVisClient!!)

        }

    }


    private fun ReadFromUrl(client: ComputerVisionClient) {
        Log.d(TAG, "ReadFromUrl: ")
        val remoteTextImageURL = "https://intelligentkioskstore.blob.core.windows.net/visionapi/suggestedphotos/3.png"
        println("Read with URL: $remoteTextImageURL")
        try {
            // Cast Computer Vision to its implementation to expose the required methods
            val vision = client.computerVision() as ComputerVisionImpl

            // Read in remote image and response header
            val responseHeader = vision.readWithServiceResponseAsync(remoteTextImageURL, null, null, null)
                    .toBlocking()
                    .single()
                    .headers()

            // Extract the operation Id from the operationLocation header
            val operationLocation = responseHeader.operationLocation()
            println("Operation Location:$operationLocation")
            getAndPrintReadResult(vision, operationLocation)
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "ReadFromUrl: Error:${e.message}")
            e.printStackTrace()
        }
    }

    // <snippet_auth>
    fun Authenticate(subscriptionKey: String?, endpoint: String?): ComputerVisionClient? {
        return ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint)
    }

    /**
     * OCR with READ : Performs a Read Operation on a local image
     * @param client instantiated vision client
     * @param localFilePath local file path from which to perform the read operation against
     */
    private fun ReadFromFile(client: ComputerVisionClient, byteArray: ByteArray) {
        Log.d(TAG, "ReadFromFile: ")
        val localFilePath = "src\\main\\resources\\myImage.png"
        println("Read with local file: $localFilePath")
        // </snippet_read_setup>
        // <snippet_read_call>
        try {
            val localImageBytes: ByteArray = byteArray

            // Cast Computer Vision to its implementation to expose the required methods
            val vision = client.computerVision() as ComputerVisionImpl

            // Read in remote image and response header
            val responseHeader = vision.readInStreamWithServiceResponseAsync(localImageBytes, null, null)
                    .toBlocking()
                    .single()
                    .headers()
            // </snippet_read_call>
            // <snippet_read_response>
            // Extract the operationLocation from the response header
            val operationLocation = responseHeader.operationLocation()
            println("Operation Location:$operationLocation")
            getAndPrintReadResult(vision, operationLocation)
            // </snippet_read_response>
            // <snippet_read_catch>
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e(TAG, "ReadFromFile: Error:${e.message}", e)
        }
    }
    // </snippet_read_catch>

    // <snippet_opid_extract>
    // </snippet_read_catch>
    // <snippet_opid_extract>
    /**
     * Extracts the OperationId from a Operation-Location returned by the POST Read operation
     * @param operationLocation
     * @return operationId
     */
    private fun extractOperationIdFromOpLocation(operationLocation: String?): String {
        if (operationLocation != null && !operationLocation.isEmpty()) {
            val splits = operationLocation.split("/".toRegex()).toTypedArray()
            if (splits != null && splits.size > 0) {
                return splits[splits.size - 1]
            }
        }
        throw IllegalStateException("Something went wrong: Couldn't extract the operation id from the operation location")
    }
    // </snippet_opid_extract>

    // <snippet_read_result_helper_call>
    // </snippet_opid_extract>
    // <snippet_read_result_helper_call>
    /**
     * Polls for Read result and prints results to console
     * @param vision Computer Vision instance
     * @return operationLocation returned in the POST Read response header
     */
    @Throws(InterruptedException::class)
    private fun getAndPrintReadResult(vision: ComputerVision, operationLocation: String) {
        Log.d(TAG, "getAndPrintReadResult:Polling for Read results ... ")

        // Extract OperationId from Operation Location
        val operationId = extractOperationIdFromOpLocation(operationLocation)
        var pollForResult = true
        var readResults: ReadOperationResult? = null
        while (pollForResult) {
            // Poll for result every second
            Thread.sleep(1000)
            readResults = vision.getReadResult(UUID.fromString(operationId))

            // The results will no longer be null when the service has finished processing the request.
            if (readResults != null) {
                // Get request status
                val status = readResults.status()
                if (status == OperationStatusCodes.FAILED || status == OperationStatusCodes.SUCCEEDED) {
                    pollForResult = false
                }
            }
        }
        // </snippet_read_result_helper_call>

        // <snippet_read_result_helper_print>
        // Print read results, page per page
        for (pageResult in readResults!!.analyzeResult().readResults()) {
            println("")
            println("Printing Read results for page " + pageResult.page())
            val builder = StringBuilder()
            for (line in pageResult.lines()) {
                builder.append(line.text())
                builder.append("\n")
            }
            Log.d(TAG, "getAndPrintReadResult: results:${builder.toString()}")
        }
    }
    // </snippet_read_res

    //////////azure
    private val _analysesStatus = Channel<Resource<String>>()
    val analysesStatus = _analysesStatus.receiveAsFlow()

    private suspend fun startAnalysis(inkBuilder: Ink.Builder, width: Float, height: Float) {
        Log.d(TAG, "startAnalysis: ")
        if (!modelIsPresent) {
            _analysesStatus.send(Resource.error(java.lang.Exception("Model is Absent")))
            return
        }

        val recognitionContext: RecognitionContext =
                RecognitionContext.builder()
                        .setPreContext("hello")
                        .setWritingArea(WritingArea(width, height))
                        .build()

        val recognizer = getDigitalInkRecognizer()
        _analysesStatus.send(Resource.loading("Analysing..."))
        recognizer.recognize(inkBuilder.build(), recognitionContext)
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
        var problem = Problem(first = getCurrentNumber().first, second = getCurrentNumber().second)
        if (answerIsCorrect(writtenAnswers, correctAnswer)) {
            Log.d(TAG, "answerReceived: correct")
            correctCount++

            problem = problem.copy(answer = correctAnswer.toString())
            correctList.add(problem)
        } else {
            Log.d(TAG, "answerReceived: wrong")


            problem = problem.copy(answer = writtenAnswers[0].text)
            wrongList.add(problem)

        }
        counter++
        if (counter < getData.value.data!!.size) {
            _additionEvents.send(Event.Next)
        } else {
            if (correctCount >= numberOfCorrect) {
                _additionEvents.send(Event.FinishedPassed(correctList, wrongList))

            } else {
                _additionEvents.send(Event.FinishedFailed(correctList, wrongList))

            }

        }
    }

    private fun answerIsCorrect(writtenAnswer: List<RecognitionCandidate>, correctAnswer: String): Boolean {
        writtenAnswer.forEachIndexed { index, recognitionCandidate ->
            if (recognitionCandidate.text.trim().equals(correctAnswer)) {
                Log.d(TAG, "answerReceived:${recognitionCandidate.text} :correctAnswer:$correctAnswer")
                return true
            }
            if (recognitionCandidate.text.contains(correctAnswer)) {
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
        data class StartAnalysis(val inkBuilder: Ink.Builder, val width: Float, val height: Float) : Event()
        data class StartAnalysisAzure(val path: String) : Event()
        object Next : Event()
        data class FinishedPassed(val correctList: MutableList<Problem>, val wrongList: MutableList<Problem>) : Event()
        data class FinishedFailed(val correctList: MutableList<Problem>, val wrongList: MutableList<Problem>) : Event()
    }
}