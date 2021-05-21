package com.example.edward.nyansapo.assess_process.paragraph

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
import com.example.edward.nyansapo.assess_process.paragraph.ParagraphAssessmentFragment.*
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

class ParagraphAssessmentViewModel constructor(@ApplicationContext private val context: Context, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val TAG = "ParagraphAssessmentView"

    private val _paragraphAssessmentEvents = Channel<ParagraphAssessmentFragment.Event>()
    val paragraphAssessmentEvents = _paragraphAssessmentEvents.receiveAsFlow()
    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.SentPermission -> {
                    sendPermission(event.granted)
                }
                is Event.SkipClicked -> {
                    _paragraphAssessmentEvents.send(Event.SkipClicked)
                }
                is Event.RecordClicked -> {
                    _paragraphAssessmentEvents.send(Event.RecordClicked)
                }
                is Event.InitTranscription -> {
                    initTranscription()
                }
                is Event.FetchParagraph -> {
                    fetchParagraph()
                }
                is Event.FetchSentence -> {
                    fetchSentence()
                }

            }
        }
    }

    private val _fetchSentenceStatus = Channel<Resource<String>>()
    val fetchSentenceStatus = _fetchSentenceStatus.receiveAsFlow()

    private suspend fun fetchSentence() {
        _fetchSentenceStatus.send(Resource.success(sentences[sentence_count]))
    }

    lateinit var sentences: Array<String>

    var error_count = 0
    var sentence_count = 0
    var tries = 0
    private val _fetchParagraphStatus = Channel<Resource<Boolean>>()
    val fetchParagraphStatus = _fetchParagraphStatus.receiveAsFlow()
    private suspend fun fetchParagraph() {
        val assessment = savedStateHandle.get<Assessment>(ASSESSMENT_ARG)!!
        val paragraphs = getPara(assessment.assessmentKey.toString())
        val paragraph = paragraphs[assessment.paragraphChoosen]

        sentences = paragraph!!.split(".").map {
            it.trim()
        }.filter { line ->
            line.isNotBlank()
        }.toTypedArray()
        _fetchParagraphStatus.send(Resource.success(true))


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
        _paragraphAssessmentEvents.send(Event.StopRecording)
    }

    var paragraph_words_wrong = ""

    private suspend fun transcriptionSuccess(textFromServer: String) {
        Log.d(TAG, "transcriptionSuccess:textFromServer:$textFromServer ")
        val expectedTextCleaned = sentences[sentence_count].cleanTranscriptionTxt

        val textFromServerCleaned = textFromServer.cleanTranscriptionTxt

        val listOfWordsFromTxtFromServer = textFromServer.sentenceToList

        val listOfExpectedWords = expectedTextCleaned.sentenceToList


        Log.d(TAG, "onPostExecute: list of words expected listOfWordsFromExpectedTxt: $listOfExpectedWords")
        (listOfExpectedWords as ArrayList).removeAll(listOfWordsFromTxtFromServer)
        val countErrorFromSentence = listOfExpectedWords.size

        var error_txt = ""
        listOfExpectedWords.forEach {
            error_txt += it + ","
            Log.d(TAG, "onPostExecute: error_text:$error_txt")

        }


        if (countErrorFromSentence > 2 || listOfWordsFromTxtFromServer.size < 2) {

            if (tries < 1) {
                tries++

                _transcriptionsStatus.send(Resource.error(Exception("Try Again")))
            } else {
                error_count += countErrorFromSentence
                paragraph_words_wrong += error_txt.trim()
                Log.d(TAG, "onPostExecute: paragraph_words_wrong: $paragraph_words_wrong ")



                changeSentence(textFromServer)
            }
        } else {
            error_count += countErrorFromSentence
            Log.d(TAG, "onPostExecute: error_count: $error_count : countErrorFromSentence: $countErrorFromSentence")

            paragraph_words_wrong += error_txt.trim()
            Log.d(TAG, "onPostExecute: paragraph_words_wrong: $paragraph_words_wrong")

            Log.d(TAG, "onPostExecute: paragraph_words_wrong: $paragraph_words_wrong")
            changeSentence(textFromServer)
        }
    }


    private suspend fun changeSentence(textFromServer: String) {
        tries = 0 // everytime a sentence is changed tries go to one
        if (sentence_count < sentences.size - 1) {
            sentence_count += 1 // increment sentence count
            fetchSentence()
        } else { // move to another  level //only if we have finished reading all the paragraph containing 4 sentences
            if (error_count > 2) {
                goToWord()
            } else {
                goToStory()
            }
        }

    }

    private suspend fun goToStory() {
        val assessment = savedStateHandle.get<Assessment>(ASSESSMENT_ARG)!!
        assessment!!.paragraphWordsWrong = paragraph_words_wrong // set words wrong
        _paragraphAssessmentEvents.send(Event.GoToStory(assessment))
    }

    private suspend fun goToWord() {
        val assessment = savedStateHandle.get<Assessment>(ASSESSMENT_ARG)!!
        assessment!!.paragraphWordsWrong = paragraph_words_wrong // set words wrong
        _paragraphAssessmentEvents.send(Event.GoToWord(assessment))
    }

    private suspend fun sendPermission(granted: Boolean) {
        if (granted) {
            _paragraphAssessmentEvents.send(Event.PermissionGranted)
        } else {
            _paragraphAssessmentEvents.send(Event.PermissionDenied)

        }
    }

    fun getPara(key: String?): Array<String> {
        return when (key) {
            "3" -> {
                Assessment_Content.getP3()
            }
            "4" -> {
                Assessment_Content.getP4()
            }
            "5" -> {
                Assessment_Content.getP5()
            }
            "6" -> {
                Assessment_Content.getP6()
            }
            "7" -> {
                Assessment_Content.getP7()
            }
            "8" -> {
                Assessment_Content.getP8()
            }
            "9" -> {
                Assessment_Content.getP9()
            }
            "10" -> {
                Assessment_Content.getP10()
            }
            else -> Assessment_Content.getP3()
        }
    }

}