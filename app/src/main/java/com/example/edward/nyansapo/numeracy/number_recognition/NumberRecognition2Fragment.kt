package com.example.edward.nyansapo.numeracy.number_recognition

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentNumberRecognition2Binding
import com.example.edward.nyansapo.wrappers.Resource
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutionException

@AndroidEntryPoint
class NumberRecognition2Fragment : Fragment(R.layout.fragment_number_recognition_2) {

    private val TAG = "NumberRecognition2Fragm"
    private val RC_PERMISSION = 9
    private val viewModel: NumberRecognitionViewModel by viewModels()
    lateinit var binding: FragmentNumberRecognition2Binding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNumberRecognition2Binding.bind(view)
        subScribeToObservers()
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getNumberRecogn_2.collect {
                    when (it.status) {
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.SUCCESS -> {
                            setOnClickListeners()
                            displayNumber()

                        }
                        Resource.Status.ERROR -> {

                        }
                    }
                }
            }

        }
    }

    private fun displayNumber() {
        Log.d(TAG, "displayNumber: getNumberRecogn_2:${viewModel.getNumberRecogn_2}")
        Log.d(TAG, "displayNumber: getCurrentNumber:${viewModel.getCurrentNumber()}")
        Log.d(TAG, "displayNumber: counter:${viewModel.counter}")
        Log.d(TAG, "displayNumber: correctCount:${viewModel.correctCount}")
        binding.numberRecognTxtView.setBackgroundResource(R.drawable.bg_number_recognition_not_recording)
        binding.numberRecognTxtView.text = viewModel.getCurrentNumber().toString()
    }

    private fun setOnClickListeners() {
        binding.imvMic.setOnClickListener {
            micClicked()
        }
        binding.imvAvatar.setOnClickListener {
            avatarClicked()
        }
        binding.skipTxtView.setOnClickListener {
            findNavController().navigate(R.id.action_numberRecognition2Fragment_to_additionFragment)
        }
    }

    private fun avatarClicked() {

    }


    private fun micClicked() {
        checkIfWeHavePermissions()

    }

    private fun checkIfWeHavePermissions() {
        Log.d(TAG, "checkIfWeHavePermissions: ")

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.RECORD_AUDIO), RC_PERMISSION)
            Log.d(TAG, "checkIfWeHavePermissions: permission not available")
        } else {
            Log.d(TAG, "checkIfWeHavePermissions: permissions available")
            recordStudent()
        }
    }

    private fun recordStudent() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.numberRecognTxtView.setBackgroundResource(R.drawable.bg_number_recognition_recording)
            launch(Dispatchers.IO) {
                startRecording()

            }

        }
    }

    private suspend fun startRecording() {
        Log.d(TAG, "startRecording: ")
        val speechSubscriptionKey = requireActivity().getString(R.string.speech_subscription_key)
        val serviceRegion = requireActivity().getString(R.string.service_region)
        val config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion)

        config!!.endpointId = requireActivity().getString(R.string.end_point)
        val reco = SpeechRecognizer(config)
        var result: SpeechRecognitionResult? = null
        val task = reco!!.recognizeOnceAsync()!!


        try {
            result = task.get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        if (result!!.reason == ResultReason.RecognizedSpeech) {
            withContext(Dispatchers.Main) {
                successResult(result.text)
            }

        } else if (result.reason == ResultReason.NoMatch) {
            showToastInfo("No Match")
        } else if (result.reason == ResultReason.Canceled) {
            showToastInfo("Cancelled")

        }

    }

    private fun successResult(result: String) {
        Log.d(TAG, "successResult: result:$result")
        val cleanResult = result.cleanResult
        Log.d(TAG, "successResult: cleanResult:$cleanResult")
        Log.d(TAG, "successResult: getNumberRecogn_2:${viewModel.getCurrentNumber()}::cleanResult :$cleanResult")
        if (viewModel.getCurrentNumber().toString() == cleanResult) {
            Log.d(TAG, "successResult: correct")
            viewModel.correctCount++
        } else {
            Log.d(TAG, "successResult: wrong")

        }
        goToNext()
    }

    private fun goToNext() {


        viewModel.counter++
        displayNumber()


    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

}

val String.cleanResult get() = this.replace(".", "")