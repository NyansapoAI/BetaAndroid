package com.example.edward.nyansapo.numeracy.word_problem

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
import com.edward.nyansapo.databinding.FragmentWordProblemBinding
import com.example.edward.nyansapo.numeracy.AssessmentNumeracy
import com.example.edward.nyansapo.numeracy.Numeracy_Learning_Levels
import com.example.edward.nyansapo.numeracy.addition.AdditionViewModel_2
import com.example.edward.nyansapo.numeracy.subtraction.SubtractionFragmentDirections
import com.example.edward.nyansapo.numeracy.word_problem.WordProblemViewModel.*
import com.example.edward.nyansapo.util.GlobalData
import com.example.edward.nyansapo.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_thank_you.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WordProblemFragment : Fragment(R.layout.fragment_word_problem) {

    private val TAG = "WordProblemFragment"

    private val viewModel: WordProblemViewModel by viewModels()
    private val navArgs: WordProblemFragmentArgs by navArgs()
    private lateinit var binding: FragmentWordProblemBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: assessmentNumeracy:${navArgs.assessmentNumeracy}")
        initProgressBar()
        binding = FragmentWordProblemBinding.bind(view)
        subScribeToObservers()
        setDefaults()
    }

    private fun setDefaults() {
        binding.imvAvatar.setImageResource(navArgs.assessmentNumeracy.student.avatar)

    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getWordProblem.collect {
                    Log.d(TAG, "subScribeToObservers: getWordProblem:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.SUCCESS -> {
                            initUi(it.data!!)
                            setOnClickListeners()

                        }
                        Resource.Status.ERROR -> {

                        }
                    }
                }
            }
            launch {
                viewModel.analysesStatus.collect {
                    Log.d(TAG, "subScribeToObservers: analysesStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
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
                viewModel.wordProblemEvents.collect {
                    when (it) {

                        is Event.FinishedPassed -> {
                            finishedPassed(it.correctAnswer, it.writtenAnwer)
                        }
                        is Event.FinishedFailed -> {
                            finishedFailed(it.correctAnswer, it.writtenAnwer)
                        }
                    }
                }
            }
        }


    }


    private fun finishedPassed(correctAnswer: Int, writtenAnwer: String) {
        val student = navArgs.assessmentNumeracy.student

        val assessmentNumeracy = navArgs.assessmentNumeracy.copy(wordProblemIsCorrect = true, correctWordProblemAnswer = correctAnswer, wrongWordProblemAnswer = writtenAnwer)
        if (assessmentNumeracy.learningLevelNumeracy.equals(Numeracy_Learning_Levels.UNKNOWN.name)) {
            student.learningLevelNumeracy = Numeracy_Learning_Levels.ABOVE.name
            assessmentNumeracy.learningLevelNumeracy = Numeracy_Learning_Levels.ABOVE.name
        }
        goTo(assessmentNumeracy.copy(student = student))

    }

    private fun finishedFailed(correctAnswer: Int, writtenAnwer: String) {
        val student = navArgs.assessmentNumeracy.student

        val assessmentNumeracy = navArgs.assessmentNumeracy.copy(wordProblemIsCorrect = false, correctWordProblemAnswer = correctAnswer, wrongWordProblemAnswer = writtenAnwer)
        if (assessmentNumeracy.learningLevelNumeracy.equals(Numeracy_Learning_Levels.UNKNOWN.name)) {
            student.learningLevelNumeracy = Numeracy_Learning_Levels.DIVISION.name
            assessmentNumeracy.learningLevelNumeracy = Numeracy_Learning_Levels.DIVISION.name
        }
        goTo(assessmentNumeracy.copy(student = student))
    }

    private fun goTo(assessmentNumeracy: AssessmentNumeracy) {
        findNavController().navigate(WordProblemFragmentDirections.actionWordProblemFragmentToThankYouFragment2(assessmentNumeracy))
    }


    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun setOnClickListeners() {
        binding.imvAvatar.setOnClickListener {
            viewModel.setEvent(Event.StartAnalysis(binding.answerTxtView.inkBuilder))
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

    private fun initUi(data: Pair<String, String>) {
        binding.problemTxtView.text = data.first
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

        dialog = setProgressDialog(requireContext(), "Loading..")
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