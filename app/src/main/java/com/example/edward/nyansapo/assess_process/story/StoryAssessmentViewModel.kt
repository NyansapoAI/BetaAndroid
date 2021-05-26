package com.example.edward.nyansapo.assess_process.story

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.Assisted
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edward.nyansapo.R
import com.example.edward.nyansapo.*
import com.example.edward.nyansapo.assess_process.paragraph.ParagraphAssessmentFragment
import com.example.edward.nyansapo.assess_process.story.StoryAssessmentFragment.*
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


class StoryAssessmentViewModel constructor(@ApplicationContext private val context: Context, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {


    private val TAG = "StoryAssessmentViewMode"

    private val _storyAssessmentEvents = Channel<Event>()
    val storyAssessmentEvents = _storyAssessmentEvents.receiveAsFlow()
    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.SentPermission -> {
                    sendPermission(event.granted)
                }
                is Event.SkipClicked -> {
                    _storyAssessmentEvents.send(Event.SkipClicked)
                }
                is Event.RecordClicked -> {
                    _storyAssessmentEvents.send(Event.RecordClicked)
                }
                is Event.InitTranscription -> {
                    initTranscription()
                }
                is Event.FetchStory -> {
                    fetchStory()
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
        _fetchSentenceStatus.send(Resource.success(sentenceList[sentence_count]))
    }

    lateinit var sentenceList: Array<String>

    var error_count = 0
    var sentence_count = 0
    var tries = 0
    private val _fetchStoryStatus = Channel<Resource<Boolean>>()
    val fetchParagraphStatus = _fetchStoryStatus.receiveAsFlow()
    private suspend fun fetchStory() {
        val assessment = savedStateHandle.get<Assessment>(ASSESSMENT_ARG)!!
        val storyString = getStory(assessment.assessmentKey.toString())

        sentenceList = storyString!!.split(".").map {
            it.trim()
        }.filter { line ->
            line.isNotBlank()
        }.toTypedArray()
        _fetchStoryStatus.send(Resource.success(true))


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
        _storyAssessmentEvents.send(Event.StopRecording)
    }

    var story_words_wrong = ""

    private suspend fun transcriptionSuccess(textFromServer: String) {
        Log.d(TAG, "transcriptionSuccess:textFromServer:$textFromServer ")
        val expectedTextCleaned = sentenceList[sentence_count].cleanTranscriptionTxt

        val textFromServerCleaned = textFromServer.cleanTranscriptionTxt

        val listOfWordsFromServer = textFromServer.sentenceToList

        val listOfExpectedWords = expectedTextCleaned.sentenceToList


        Log.d(TAG, "onPostExecute: list of words expected listOfWordsFromExpectedTxt: $listOfExpectedWords")
        (listOfExpectedWords as ArrayList).removeAll(listOfWordsFromServer)
        val countErrorFromSentence = listOfExpectedWords.size

        var error_txt = ""
        listOfExpectedWords.forEach {
            error_txt += it + ","
            Log.d(TAG, "onPostExecute: error_text:$error_txt")

        }


        val dummy_error_count = error_count + countErrorFromSentence

        if (dummy_error_count > 10) { // if error less than 8 move to story level
            story_words_wrong += error_txt
            Log.d(TAG, "onPostExecute: error_count is greater than 10 :dummy_error_count:$dummy_error_count")
            goToThankYou()
        } else {
            if (countErrorFromSentence > 3 || listOfWordsFromServer.size < 2) {

                if (tries < 1) {
                    tries++

                    _transcriptionsStatus.send(Resource.error(Exception("Try Again")))
                } else {
                    error_count += countErrorFromSentence
                    story_words_wrong += error_txt.trim()
                    Log.d(TAG, "onPostExecute: paragraph_words_wrong: $story_words_wrong ")

                    changeSentence(textFromServer)
                }
            } else {
                error_count += countErrorFromSentence
                Log.d(TAG, "onPostExecute: error_count: $error_count : countErrorFromSentence: $countErrorFromSentence")

                story_words_wrong += error_txt.trim()
                Log.d(TAG, "onPostExecute: paragraph_words_wrong: $story_words_wrong")

                Log.d(TAG, "onPostExecute: paragraph_words_wrong: $story_words_wrong")
                changeSentence(textFromServer)
            }
        }
    }

    private suspend fun goToThankYou() {
        val assessment = savedStateHandle.get<Assessment>(ASSESSMENT_ARG)!!
        assessment!!.learningLevel = Learning_Level.PARAGRAPH.name
        assessment!!.storyWordsWrong = story_words_wrong
        _storyAssessmentEvents.send(Event.GoToThankYou(assessment))

    }


    private suspend fun changeSentence(textFromServer: String) {
        tries = 0 // everytime a sentence is changed tries go to one
        if (sentence_count < sentenceList.size - 1) {
            sentence_count += 1 // increment sentence count
            fetchSentence()
        } else { // move to another  level //only if we have finished reading all the paragraph containing 4 sentences
            goToStoryQuestions()
        }

    }

    private suspend fun goToStoryQuestions() {
        val assessment = savedStateHandle.get<Assessment>(ASSESSMENT_ARG)!!
        assessment!!.storyWordsWrong = story_words_wrong
        val question = 0
        _storyAssessmentEvents.send(Event.GoToStoryQuestion(assessment, question))
    }


    private suspend fun sendPermission(granted: Boolean) {
        if (granted) {
            _storyAssessmentEvents.send(Event.PermissionGranted)
        } else {
            _storyAssessmentEvents.send(Event.PermissionDenied)

        }
    }

    fun getStory(key: String?): String {
        return when (key) {
            "3" -> {
                Assessment_Content.getS3()
            }
            "4" -> {
                Assessment_Content.getS4()
            }
            "5" -> {
                Assessment_Content.getS5()
            }
            "6" -> {
                Assessment_Content.getS6()
            }
            "7" -> {
                Assessment_Content.getS7()
            }
            "8" -> {
                Assessment_Content.getS8()
            }
            "9" -> {
                Assessment_Content.getS9()
            }
            "10" -> {
                Assessment_Content.getS10()
            }
            else -> Assessment_Content.getS3()
        }
    }

}