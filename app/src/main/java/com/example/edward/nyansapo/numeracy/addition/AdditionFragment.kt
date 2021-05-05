package com.example.edward.nyansapo.numeracy.addition

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
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentAdditionBinding
import com.example.edward.nyansapo.wrappers.Resource
import com.google.mlkit.vision.digitalink.Ink
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdditionFragment : Fragment(R.layout.fragment_addition) {

    private val TAG = "AdditionFragment"

    private lateinit var binding: FragmentAdditionBinding
    private val viewModel: AdditionViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdditionBinding.bind(view)
        initProgressBar()
        subScribeToObservers()

    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getAddition.collect {
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
                        is Event.Next -> {
                            goToNext()
                        }
                        is Event.Finished -> {
                            finished()
                        }
                    }
                }
            }
        }
    }

    private fun finished() {
        findNavController().navigate(R.id.action_additionFragment_to_subtractionFragment)

    }

    private fun modelIsAbsent() {
        viewModel.setEvent(Event.StartModelDownload)
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun setOnClickListener() {
        binding.imageViewAvatar.setOnClickListener {
            viewModel.setEvent(Event.StartAnalysis(binding.answerTxtView.inkBuilder))
        }

        binding.skipTxtView.setOnClickListener {
            findNavController().navigate(R.id.action_additionFragment_to_wordProblemFragment)
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
            symbolTxtView.text = "+"
        }
    }

    sealed class Event {
        object RecordStudent : Event()
        data class CheckIfCorrect(val recorded: String) : Event()
        object CheckIfModelIsDownloaded : Event()
        object StartModelDownload : Event()
        data class StartAnalysis(val inkBuilder: Ink.Builder) : Event()
        object Next : Event()
        object Finished : Event()
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