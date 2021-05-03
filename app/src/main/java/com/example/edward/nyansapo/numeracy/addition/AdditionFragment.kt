package com.example.edward.nyansapo.numeracy.addition

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentAdditionBinding
import com.example.edward.nyansapo.wrappers.Resource
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class AdditionFragment : Fragment(R.layout.fragment_addition) {

    private val TAG = "AdditionFragment"

    private lateinit var binding: FragmentAdditionBinding
    private val viewModel: AdditionViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdditionBinding.bind(view)
        subScribeToObservers()

    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getAddition.collect {
                    when (it.status) {
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.SUCCESS -> {
                            initUi()
                            setOnClickListener()

                        }
                        Resource.Status.ERROR -> {

                        }
                    }
                }
            }
        }
    }

    private fun setOnClickListener() {
        binding.imageViewAvatar.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                if (checkIfModelIsDownloaded()) {
                    Log.d(TAG, "setOnClickListener: model is downloaded")
                    startAnalysis()
                } else {
                    Log.d(TAG, "setOnClickListener: model is not download")
                    downloadModel()
                }
            }


        }
    }

    private suspend fun checkIfModelIsDownloaded(): Boolean {
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

        return remoteModelManager.isModelDownloaded(model).await()
    }

    private fun downloadModel() {
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

        remoteModelManager.download(model, DownloadConditions.Builder().build())
                .addOnSuccessListener {
                    Log.i(TAG, "Model downloaded")
                }
                .addOnFailureListener { e: Exception ->
                    Log.e(TAG, "Error while downloading a model: $e")
                    e.printStackTrace()

                }
    }

    private fun startAnalysis() {
        Log.d(TAG, "startAnalysis: ")
        val recognizer = getDigitalInkRecognizer()
        recognizer.recognize(binding.answerTxtView.inkBuilder.build())
                .addOnSuccessListener { result: RecognitionResult ->
                    // `result` contains the recognizer's answers as a RecognitionResult.
                    // Logs the text from the top candidate.

                    Log.d(TAG, "startAnalysis:Best Result:: ${result.candidates[0].text}")

                    result.candidates.forEachIndexed { index, recognitionCandidate ->
                        Log.d(TAG, "startAnalysis: index:$index Result:${result.candidates[index].text}")

                    }

                }


                .addOnFailureListener { e: Exception ->
                    Log.d(TAG, "startAnalysis: Error:${e.message}")
                    Log.e(TAG, "Error during recognition: $e")
                    e.printStackTrace()
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

    private fun initUi() {
        displayNumbers()
    }

    private fun displayNumbers() {
        binding.apply {
            firstNumberTxtView.text = viewModel.getCurrentNumber().first.toString()
            secondNumberTxtView.text = viewModel.getCurrentNumber().second.toString()
            symbolTxtView.text = "+"
        }
    }
}