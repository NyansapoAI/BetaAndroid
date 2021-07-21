package com.example.edward.nyansapo.numeracy.multiplication

import android.app.AlertDialog
import android.content.Context
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentAdditionBinding
import com.example.edward.nyansapo.numeracy.AssessmentNumeracy
import com.example.edward.nyansapo.numeracy.Numeracy_Learning_Levels
import com.example.edward.nyansapo.numeracy.Operators
import com.example.edward.nyansapo.numeracy.Problem
import com.example.edward.nyansapo.numeracy.addition.AdditionViewModel_2
import com.example.edward.nyansapo.numeracy.subtraction.SubtractionFragmentArgs
import com.example.edward.nyansapo.util.GlobalData
import com.example.edward.nyansapo.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MultiplicationFragment : Fragment(R.layout.fragment_addition) {


    private val TAG = "MultiplicationFragment"


    private lateinit var binding: FragmentAdditionBinding
    private val viewModel: AdditionViewModel_2 by viewModels()
    private val navArgs: SubtractionFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: assessmentNumeracy:${navArgs.assessmentNumeracy}")

        binding = FragmentAdditionBinding.bind(view)
        initProgressBar()
        viewModel.setOperation(Operators.MULTIPLICATION)
        setDefaults()
        subScribeToObservers()

    }

    private fun setDefaults() {
        binding.imvAvatar.setImageResource(navArgs.assessmentNumeracy.student.avatar)


        binding.tvHeader.text = "Multiplication"
        binding.tvSymbol.text = "*"
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {

            launch {
                viewModel.getData.collect {
                    Log.d(TAG, "subScribeToObservers: getAddition:${it.status.name}")
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

            launch {
                viewModel.modelPresentStatus.collect {
                    Log.d(TAG, "subScribeToObservers: modelPresentStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            initProgressBar(it.message!!)
                            showProgress(true)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                            modelIsAbsent()
                        }
                    }
                }
            }
            launch {
                viewModel.modelDownloadStatus.collect {
                    Log.d(TAG, "subScribeToObservers: modelDownloadStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            initProgressBar(it.message!!)
                            showProgress(true)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                            showToastInfo("Error: ${it.exception?.message}")
                        }
                    }
                }
            }
            launch {
                viewModel.analysesStatus.collect {
                    Log.d(TAG, "subScribeToObservers: analysesStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            initProgressBar(it.message!!)
                            showProgress(true)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                            showToastInfo("Error: ${it.exception?.message}")
                        }
                    }
                }
            }
            launch {
                viewModel.additionEvents.collect {
                    when (it) {
                        is AdditionViewModel_2.Event.Next -> {
                            goToNext()
                        }
                        is AdditionViewModel_2.Event.FinishedPassed -> {
                            finishedPassed(it.correctList, it.wrongList)
                        }
                        is AdditionViewModel_2.Event.FinishedFailed -> {
                            finishedFailed(it.correctList, it.wrongList)
                        }
                    }
                }
            }
        }
    }

    private fun finishedFailed(correctList: MutableList<Problem>, wrongList: MutableList<Problem>) {
        val student = navArgs.assessmentNumeracy.student
        val assessmentNumeracy = navArgs.assessmentNumeracy.copy(correctMultiplication = viewModel.correctCount, correctMultiplicationList = correctList, wrongMultiplicationList = wrongList, student = student)

        if (assessmentNumeracy.learningLevelNumeracy.equals(Numeracy_Learning_Levels.UNKNOWN.name)) {
            student.learningLevelNumeracy = Numeracy_Learning_Levels.MULTIPLICATION.name
            assessmentNumeracy.learningLevelNumeracy = Numeracy_Learning_Levels.MULTIPLICATION.name

        }
         goTo(assessmentNumeracy.copy(student=student))

    }

    private fun finishedPassed(correctList: MutableList<Problem>, wrongList: MutableList<Problem>) {
        val assessmentNumeracy = navArgs.assessmentNumeracy.copy(correctMultiplication = viewModel.correctCount, correctMultiplicationList = correctList, wrongMultiplicationList = wrongList)
        goTo(assessmentNumeracy)
    }

    private fun goTo(assessmentNumeracy: AssessmentNumeracy) {
        findNavController().navigate(MultiplicationFragmentDirections.actionMultiplicationFragmentToDivisionFragment(assessmentNumeracy))
    }

    private fun modelIsAbsent() {
        viewModel.setEvent(AdditionViewModel_2.Event.StartModelDownload)
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun setOnClickListener() {
        binding.imvAvatar.setOnClickListener {
            viewModel.setEvent(AdditionViewModel_2.Event.StartAnalysis(binding.answerTxtView.inkBuilder,binding.answerTxtView.width.toFloat(),binding.answerTxtView.height.toFloat()))
        }

        binding.skipTxtView.setOnClickListener {
            goTo(navArgs.assessmentNumeracy)
        }

        binding.apply {
            btnResetAnswer.setOnClickListener {
                binding.answerTxtView.clearDrawing()
            }
            btnResetWorkSpace.setOnClickListener {
                binding.root.clearDrawing()
            }
        }
    }


    private fun goToNext() {

        displayNumbers()
    }


    private fun initUi() {
        displayNumbers()
    }

    private fun displayNumbers() {
        Log.d(TAG, "displayNumber: getCurrentNumber:${viewModel.getCurrentNumber()}")
        Log.d(TAG, "displayNumber: counter:${viewModel.counter}")
        Log.d(TAG, "displayNumber: correctCount:${viewModel.correctCount}")
        binding.answerTxtView.clearDrawing()
        binding.apply {
            firstNumberTxtView.text = viewModel.getCurrentNumber().first.toString()
            secondNumberTxtView.text = viewModel.getCurrentNumber().second.toString()
        }
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

    private fun initProgressBar(message: String = "Loading..") {

        dialog = setProgressDialog(requireContext(), message)
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