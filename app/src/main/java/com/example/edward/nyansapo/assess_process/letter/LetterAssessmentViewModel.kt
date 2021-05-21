package com.example.edward.nyansapo.assess_process.letter

import android.content.Context
import android.util.Log
import androidx.hilt.Assisted
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edward.nyansapo.R
import com.example.edward.nyansapo.ASSESSMENT_ARG
import com.example.edward.nyansapo.Assessment
import com.example.edward.nyansapo.Assessment_Content
import com.example.edward.nyansapo.Learning_Level
import com.example.edward.nyansapo.assess_process.letter.LetterAssessmentFragment.*
import com.example.edward.nyansapo.assess_process.paragraph.ParagraphAssessmentFragment
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.cleanTranscriptionTxt
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.concurrent.ExecutionException


class LetterAssessmentViewModel constructor(@ApplicationContext private val context: Context, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {


    private val TAG = "LetterAssessmentViewMod"


    private val _letterAssessmentEvents = Channel<Event>()
    val letterAssessmentEvents = _letterAssessmentEvents.receiveAsFlow()
    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.SentPermission -> {
                    sendPermission(event.granted)
                }
                is Event.SkipClicked -> {
                    _letterAssessmentEvents.send(Event.SkipClicked)
                }
                is Event.RecordClicked -> {
                    _letterAssessmentEvents.send(Event.RecordClicked)
                }
                is Event.InitTranscription -> {
                    initTranscription()
                }
                is Event.FetchWords -> {
                    fetchWords()
                }
                is Event.FetchSingleWord -> {
                    fetchSingleLetter()
                }

            }
        }
    }

    private val _fetchSentenceStatus = Channel<Resource<String>>()
    val fetchSentenceStatus = _fetchSentenceStatus.receiveAsFlow()

    private suspend fun fetchSingleLetter() {
        _fetchSentenceStatus.send(Resource.success(lettersList[letter_count]))
    }


    lateinit var lettersList: Array<String>

    var error_count = 0
    var letter_count = 0

    var letters_wrong = ""
    var letters_correct = ""


    private val _fetchWordsStatus = Channel<Resource<Nothing>>()
    val fetchWordsStatus = _fetchWordsStatus.receiveAsFlow()
    private fun fetchWords() {
        val assessment = savedStateHandle.get<Assessment>(ASSESSMENT_ARG)!!
        lettersList = getLetters(assessment.assessmentKey.toString()).map { it.trim().toLowerCase() }.toTypedArray()

    }


    var transcriptionStarted = false
    private val _transcriptionsStatus = Channel<Resource<String>>()
    val transcriptionsStatus = _transcriptionsStatus.receiveAsFlow()
    private suspend fun initTranscription() {
        if (transcriptionStarted) {
            _transcriptionsStatus.send(Resource.error(Exception("Transcription Is Running Already...")))
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                startTranscription()
            }

        }
    }

    private suspend fun startTranscription() {

        _transcriptionsStatus.send(Resource.loading("transcription started..."))
        val speechSubscriptionKey = context.getString(R.string.speech_subscription_key)
        val serviceRegion = context.getString(R.string.service_region)
        val endpoint = context.getString(R.string.end_point)

        val config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion)
        config!!.setEndpointId(endpoint)


        val reco = SpeechRecognizer(config)
        var result: SpeechRecognitionResult? = null
        val task = reco!!.recognizeOnceAsync()!!
        try {
            result = task.get()
        } catch (e: ExecutionException) {
            _transcriptionsStatus.send(Resource.error(e))
            e.printStackTrace()
        } catch (e: InterruptedException) {
            _transcriptionsStatus.send(Resource.error(e))
            e.printStackTrace()
        }

        if (result!!.reason == ResultReason.RecognizedSpeech) {
            transcriptionSuccess(result.text)
            _transcriptionsStatus.send(Resource.success(result.text))
        } else if (result.reason == ResultReason.NoMatch) {
            _transcriptionsStatus.send(Resource.error(Exception("Try Again")))
        } else if (result.reason == ResultReason.Canceled) {
            _transcriptionsStatus.send(Resource.error(Exception("Internet Connection Failed")))
        }
        stopVoiceRecording()
        reco.close()

    }

    private suspend fun stopVoiceRecording() {
        _letterAssessmentEvents.send(Event.StopRecording)
    }

    var paragraph_words_wrong = ""

    private suspend fun transcriptionSuccess(textFromServer: String) {
        Log.d(TAG, "transcriptionSuccess:textFromServer:$textFromServer ")
        val expectedTextCleaned = lettersList[letter_count].cleanTranscriptionTxt

        val textFromServerCleaned = textFromServer.cleanTranscriptionTxt


        if (expectedTextCleaned.equals(textFromServerCleaned)) {
            Log.d(TAG, "onPostExecute: word is correct expected: $expectedTextCleaned text from server: $textFromServerCleaned")
            letters_correct += expectedTextCleaned!! + ","
            Log.d(TAG, "onPostExecute: correct words: $letters_correct ")
            Log.d(TAG, "onPostExecute: wrong words: $letters_wrong")
            Log.d(TAG, "onPostExecute: error_count: $error_count")
        } else {
            letters_wrong += expectedTextCleaned!! + ","
            Log.d(TAG, "onPostExecute: word is wrong expected_text: $expectedTextCleaned :textFromServerFormatted: $textFromServerCleaned")
            Log.d(TAG, "onPostExecute: wrong words: $letters_correct")
            Log.d(TAG, "onPostExecute: correct words: $letters_wrong ")
            error_count += 1
            Log.d(TAG, "onPostExecute: error_count: $error_count")
        }
        changeWord()
    }


    private suspend fun changeWord() {
        Log.d(TAG, "changeLetter: changing letter")
        Log.d(TAG, "changeLetter: error_count:$error_count")
        Log.d(TAG, "changeLetter: letter_correct: $letters_correct")
        Log.d(TAG, "changeLetter: letter_wrong: $letters_wrong")
        if (letter_count > 4) {
            Log.d(TAG, "changeLetter: letters tried greater than 4")
            goToThankYou()
        } else if (letter_count < lettersList.size - 1) {
            letter_count += 1 // increment sentence count
            fetchSingleLetter()
        } else {
            letter_count = 0 // dont know why
        }
    }


    private suspend fun goToThankYou() {
        val assessment = savedStateHandle.get<Assessment>(ASSESSMENT_ARG)!!
        assessment!!.letterCorrect = letters_correct
        assessment!!.lettersWrong = letters_wrong

        if (error_count > 2) {
            assessment!!.learningLevel = Learning_Level.BEGINNER.name
        } else {
            assessment!!.learningLevel = Learning_Level.LETTER.name
        }

        _letterAssessmentEvents.send(Event.GoToThankYou(assessment))
    }

    private suspend fun sendPermission(granted: Boolean) {
        if (granted) {
            _letterAssessmentEvents.send(Event.PermissionGranted)
        } else {
            _letterAssessmentEvents.send(Event.PermissionDenied)

        }
    }

    fun getLetters(key: String?): Array<String> {
        return when (key) {
            "3" -> {
                Assessment_Content.getL3()
            }
            "4" -> {
                Assessment_Content.getL4()
            }
            "5" -> {
                Assessment_Content.getL5()
            }
            "6" -> {
                Assessment_Content.getL6()
            }
            "7" -> {
                Assessment_Content.getL7()
            }
            "8" -> {
                Assessment_Content.getL8()
            }
            "9" -> {
                Assessment_Content.getL9()
            }
            "10" -> {
                Assessment_Content.getL10()
            }
            else -> Assessment_Content.getL3()
        }
    }


}