package com.example.edward.nyansapo.numeracy.number_recognition

import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentNumberRecognition2Binding
import com.example.edward.nyansapo.numeracy.AssessmentNumeracy
import com.example.edward.nyansapo.numeracy.Numeracy_Learning_Levels
import com.example.edward.nyansapo.numeracy.number_recognition.NumberRecognitionViewModel.*
import com.example.edward.nyansapo.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NumberRecognition2Fragment : Fragment(R.layout.fragment_number_recognition_2) {

    private val TAG = "NumberRecognition2Fragm"
    private val RC_PERMISSION = 9
    private val viewModel: NumberRecognitionViewModel by viewModels()
    private val navArgs: NumberRecognition2FragmentArgs by navArgs()
    lateinit var binding: FragmentNumberRecognition2Binding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: assessmentNumeracy:${navArgs.assessmentNumeracy}")
        binding = FragmentNumberRecognition2Binding.bind(view)
        initProgressBar()
        subScribeToObservers()
        setDefaults()
    }

    private fun setDefaults() {
        binding.imvAvatar.setImageResource(navArgs.assessmentNumeracy.student.avatar)

    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getNumberRecogn_2.collect {
                    Log.d(TAG, "subScribeToObservers: getNumberRecogn_2:${it.status.name}")
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

            launch {
                viewModel.recognitionStatus.collect {
                    Log.d(TAG, "subScribeToObservers: recognitionStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                            viewModel.setEvent(Event.CheckIfCorrect(it.data!!))
                        }
                        Resource.Status.ERROR -> {
                            displayNumber()
                            showProgress(false)
                            showToastInfo("Error: ${it.exception?.message}")
                        }
                    }
                }
            }
            launch {
                viewModel.numberRecognitionEvents.collect {
                    when (it) {
                        Event.Next -> {
                            goToNext()
                        }
                        is
                        Event.FinishedPassed -> {
                            finishedPassed(it.correctList, it.wrongList)

                        }
                        is
                        Event.FinishedFailed -> {
                            finishedFailed(it.correctList, it.wrongList)

                        }
                    }
                }
            }

        }
    }

    private fun finishedFailed(correctList: MutableList<Int>, wrongList: MutableList<Int>) {
        val student = navArgs.assessmentNumeracy.student
        val assessmentNumeracy = navArgs.assessmentNumeracy.copy(correctNumberRecognition = viewModel.correctCount, correctNumberRecognitionList = correctList, wrongNumberRecognitionList = wrongList, student = student)

        if (assessmentNumeracy.learningLevelNumeracy.equals(Numeracy_Learning_Levels.UNKNOWN.name)) {
            student.learningLevelNumeracy = Numeracy_Learning_Levels.BEGINNER.name
            assessmentNumeracy.learningLevelNumeracy = Numeracy_Learning_Levels.BEGINNER.name
        }

        goToAddition(assessmentNumeracy.copy(student=student))
    }

    private fun finishedPassed(correctList: MutableList<Int>, wrongList: MutableList<Int>) {
        val assessmentNumeracy = navArgs.assessmentNumeracy.copy(correctNumberRecognition = viewModel.correctCount, correctNumberRecognitionList = correctList, wrongNumberRecognitionList = wrongList)
        goToAddition(assessmentNumeracy)
    }

    private fun goToAddition(assessmentNumeracy: AssessmentNumeracy) {
        findNavController().navigate(NumberRecognition2FragmentDirections.actionNumberRecognition2FragmentToAdditionFragment(assessmentNumeracy))
    }

    private fun displayNumber() {
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
        binding.numberRecognTxtView.setOnClickListener {
            micClicked()
        }
        binding.imvAvatar.setOnClickListener {
            avatarClicked()
        }
        binding.skipTxtView.setOnClickListener {
            goToAddition(navArgs.assessmentNumeracy)
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
        binding.numberRecognTxtView.setBackgroundResource(R.drawable.bg_number_recognition_recording)
        viewModel.setEvent(Event.RecordStudent)


    }


    private fun goToNext() {
        displayNumber()
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }


    /////////////////////PROGRESS_BAR////////////////////////////
    lateinit var dialog: AlertDialog

    private fun showProgress(show: Boolean) {

        if (show) {
            dialog.show()

        } else {
            dialog.dismiss()

        }

    }

    private fun initProgressBar() {

        dialog = setProgressDialog(requireContext(), "Speak..")
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
    }

    fun setProgressDialog(context: Context, message: String): AlertDialog {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = message
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20.toFloat()
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }

    //end progressbar
}

val String.cleanResult get() = this.replace(".", "")