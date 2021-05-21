package com.example.edward.nyansapo.assess_process.word

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
import com.example.edward.nyansapo.assess_process.paragraph.ParagraphAssessmentFragment
import com.example.edward.nyansapo.assess_process.word.WordAssessmentFragment.*
import com.example.edward.nyansapo.util.GlobalData
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.cleanTranscriptionTxt
import com.example.edward.nyansapo.util.sentenceToList
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
import java.util.ArrayList
import java.util.concurrent.ExecutionException

class WordAssessmentViewModel constructor(@ApplicationContext private val context: Context, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {


    private val TAG = "WordAssessmentViewModel"


    private val _wordAssessmentEvents = Channel<Event>()
    val wordAssessmentEvents = _wordAssessmentEvents.receiveAsFlow()
    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.SentPermission -> {
                    sendPermission(event.granted)
                }
                is Event.SkipClicked -> {
                    _wordAssessmentEvents.send(Event.SkipClicked)
                }
                is Event.RecordClicked -> {
                    _wordAssessmentEvents.send(Event.RecordClicked)
                }
                is Event.InitTranscription -> {
                    initTranscription()
                }
                is Event.FetchWords -> {
                    fetchWords()
                }
                is Event.FetchSingleWord -> {
                    fetchSingleWord()
                }

            }
        }
    }

    private val _fetchSentenceStatus = Channel<Resource<String>>()
    val fetchSentenceStatus = _fetchSentenceStatus.receiveAsFlow()

    private suspend fun fetchSingleWord() {
        _fetchSentenceStatus.send(Resource.success(wordsList[word_count]))
    }

    lateinit var wordsList: Array<String>

    var error_count = 0
    var word_count = 0

    var words_wrong = ""
    var words_correct = ""
    private val _fetchWordsStatus = Channel<Resource<Nothing>>()
    val fetchWordsStatus = _fetchWordsStatus.receiveAsFlow()
    private fun fetchWords() {
        val assessment = savedStateHandle.get<Assessment>(ASSESSMENT_ARG)!!
        wordsList = getWords(assessment.assessmentKey.toString()).map { it.trim().toLowerCase() }.toTypedArray()

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
        _wordAssessmentEvents.send(Event.StopRecording)
    }

    var paragraph_words_wrong = ""

    private suspend fun transcriptionSuccess(textFromServer: String) {
        Log.d(TAG, "transcriptionSuccess:textFromServer:$textFromServer ")
        val expectedTextCleaned = wordsList[word_count].cleanTranscriptionTxt

        val textFromServerCleaned = textFromServer.cleanTranscriptionTxt


        if (expectedTextCleaned.equals(textFromServerCleaned)) {
            Log.d(TAG, "onPostExecute: word is correct expected: $expectedTextCleaned text from server: $textFromServerCleaned")
            words_correct += expectedTextCleaned!! + ","
            Log.d(TAG, "onPostExecute: correct words: $words_correct ")
            Log.d(TAG, "onPostExecute: wrong words: $words_wrong")
            Log.d(TAG, "onPostExecute: error_count: $error_count")
        } else {
            words_wrong += expectedTextCleaned!! + ","
            Log.d(TAG, "onPostExecute: word is wrong expected_text: $expectedTextCleaned :textFromServerFormatted: $textFromServerCleaned")
            Log.d(TAG, "onPostExecute: wrong words: $words_wrong")
            Log.d(TAG, "onPostExecute: correct words: $words_correct ")
            error_count += 1

            Log.d(TAG, "onPostExecute: error_count: $error_count")
        }
        changeWord()
    }


    private suspend fun changeWord() {
        Log.d(TAG, "changeWord: ")
        Log.d(TAG, "changeWord: error_count:$error_count")

        if (word_count > 4) { // if 6 has been tried
            Log.d(TAG, "changeWord: error_count:$error_count")
            if (error_count < 2) {
                Log.d(TAG, "changeWord:go to thank you error_count:$error_count less than 2")
                Log.d(TAG, "changeWord: words_correct:$words_correct")
                Log.d(TAG, "changeWord: words_wrong:$words_wrong")

                goToThankYou()
            } else {
                Log.d(TAG, "changeWord:go to letter  error_count:$error_count more than 2")
                Log.d(TAG, "changeWord: words_correct:$words_correct")
                Log.d(TAG, "changeWord: words_wrong:$words_wrong")
                goToLetter()
            }
        } else if (word_count < wordsList.size - 1) {
            word_count += 1 // increment sentence count
            fetchSingleWord()

            Log.d(TAG, "changeWord: words_correct:$words_correct")
            Log.d(TAG, "changeWord: words_wrong:$words_wrong")

            Log.d(TAG, "changeWord: word_count:$word_count number of words:${wordsList.size - 1}")
        } else {
            word_count = 0 // dont know why
        }
    }


    private suspend fun goToThankYou() {
        val assessment = savedStateHandle.get<Assessment>(ASSESSMENT_ARG)!!
        assessment!!.wordsWrong = words_wrong
        assessment!!.wordsCorrect = words_correct
        assessment!!.learningLevel = "WORD"
        _wordAssessmentEvents.send(Event.GoToThankYou(assessment))
    }

    private suspend fun goToLetter() {
        val assessment = savedStateHandle.get<Assessment>(ASSESSMENT_ARG)!!
        assessment!!.wordsWrong = words_wrong
        assessment!!.wordsCorrect = words_correct
        _wordAssessmentEvents.send(Event.GoToLetter(assessment))
    }


    private suspend fun sendPermission(granted: Boolean) {
        if (granted) {
            _wordAssessmentEvents.send(Event.PermissionGranted)
        } else {
            _wordAssessmentEvents.send(Event.PermissionDenied)

        }
    }

    fun getWords(key: String?): Array<String> {
        return when (key) {
            "3" -> {
                Assessment_Content.getW3()
            }
            "4" -> {
                Assessment_Content.getW4()
            }
            "5" -> {
                Assessment_Content.getW5()
            }
            "6" -> {
                Assessment_Content.getW6()
            }
            "7" -> {
                Assessment_Content.getW7()
            }
            "8" -> {
                Assessment_Content.getW8()
            }
            "9" -> {
                Assessment_Content.getW9()
            }
            "10" -> {
                Assessment_Content.getW10()
            }
            else -> Assessment_Content.getW3()
        }
    }

}