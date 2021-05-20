package com.example.edward.nyansapo.presentation.ui.preassessment

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edward.nyansapo.R
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
import java.lang.Exception
import java.util.concurrent.ExecutionException

class PreAssessmentViewModel @ViewModelInject constructor(private val repo: MainRepository, @ApplicationContext private val context: Context, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _preAssessmentEvents = Channel<PreAssessmentFragment.Event>()
    val preAssessmentEvents = _preAssessmentEvents.receiveAsFlow()
    fun setEvent(event: PreAssessmentFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is PreAssessmentFragment.Event.SentPermission -> {
                    sendPermission(event.granted)
                }
                is PreAssessmentFragment.Event.SkipClicked -> {
                    _preAssessmentEvents.send(PreAssessmentFragment.Event.SkipClicked)
                }
                is PreAssessmentFragment.Event.RecordClicked -> {
                    _preAssessmentEvents.send(PreAssessmentFragment.Event.RecordClicked)
                }
                is PreAssessmentFragment.Event.StartTranscription -> {
                    initTranscription()
                }

            }
        }
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
           _transcriptionsStatus.send(Resource.success(result.text))
        } else if (result.reason == ResultReason.NoMatch) {
            _transcriptionsStatus.send(Resource.error(Exception("No Match")))
        } else if (result.reason == ResultReason.Canceled) {
            _transcriptionsStatus.send(Resource.error(Exception("Cancelled")))
        }

    }

    private suspend fun sendPermission(granted: Boolean) {
        if (granted) {
            _preAssessmentEvents.send(PreAssessmentFragment.Event.PermissionGranted)
        } else {
            _preAssessmentEvents.send(PreAssessmentFragment.Event.PermissionDenied)

        }
    }
}