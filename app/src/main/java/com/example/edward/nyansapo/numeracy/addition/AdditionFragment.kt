package com.example.edward.nyansapo.numeracy.addition

import android.R.attr
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
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
import com.example.edward.nyansapo.numeracy.addition.AdditionViewModel_2.*
import com.example.edward.nyansapo.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.nio.file.Files


@AndroidEntryPoint
class AdditionFragment : Fragment(R.layout.fragment_addition) {

    private val TAG = "AdditionFragment"

    private lateinit var binding: FragmentAdditionBinding
    private val viewModel: AdditionViewModel_2 by viewModels()
    private val navArgs: AdditionFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: assessmentNumeracy:${navArgs.assessmentNumeracy}")
        binding = FragmentAdditionBinding.bind(view)
        initProgressBar()
        viewModel.setOperation(Operators.ADDITION)
        setDefaults()
        subScribeToObservers()

    }

    private fun setDefaults() {
        binding.imvAvatar.setImageResource(navArgs.assessmentNumeracy.student.avatar)
        binding.tvHeader.text = "Addition"
        binding.tvSymbol.text = "+"
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
                        is Event.Next -> {
                            goToNext()
                        }
                        is Event.FinishedPassed -> {
                            finishedPassed(it.correctList, it.wrongList)
                        }
                        is Event.FinishedFailed -> {
                            finishedFailed(it.correctList, it.wrongList)
                        }
                    }
                }
            }
        }
    }

    private fun finishedFailed(correctList: MutableList<Problem>, wrongList: MutableList<Problem>) {
        val student = navArgs.assessmentNumeracy.student
        val assessmentNumeracy = navArgs.assessmentNumeracy.copy(correctAddition = viewModel.correctCount, correctAdditionList = correctList, wrongAdditionList = wrongList, student = student)

        if (assessmentNumeracy.learningLevelNumeracy.equals(Numeracy_Learning_Levels.UNKNOWN.name)) {
            student.learningLevelNumeracy = Numeracy_Learning_Levels.ADDITION.name
            assessmentNumeracy.learningLevelNumeracy = Numeracy_Learning_Levels.ADDITION.name

        }
          goToSubtractionScreen(assessmentNumeracy.copy(student = student))

    }

    private fun finishedPassed(correctList: MutableList<Problem>, wrongList: MutableList<Problem>) {
        val assessmentNumeracy = navArgs.assessmentNumeracy.copy(correctAddition = viewModel.correctCount, correctAdditionList = correctList, wrongAdditionList = wrongList)
        goToSubtractionScreen(assessmentNumeracy)
    }


    private fun goToSubtractionScreen(assessmentNumeracy: AssessmentNumeracy) {
        findNavController().navigate(AdditionFragmentDirections.actionAdditionFragmentToSubtractionFragment(assessmentNumeracy))
    }

    private fun modelIsAbsent() {
        viewModel.setEvent(Event.StartModelDownload)
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun setOnClickListener() {
        binding.imvAvatar.setOnClickListener {
            viewModel.setEvent(Event.StartAnalysis(binding.answerTxtView.inkBuilder,binding.answerTxtView.width.toFloat(),binding.answerTxtView.height.toFloat()))
           // viewModel.setEvent(Event.StartAnalysisAzure("hello.txt"))

          //  startGallery()

         }

        binding.skipTxtView.setOnClickListener {
            goToSubtractionScreen(navArgs.assessmentNumeracy)
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
val GALLERY_REQUEST=5
    private fun startGallery() {
        val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        requireActivity().startActivityFromFragment(this, galleryIntent, GALLERY_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: ")
        if (requestCode === GALLERY_REQUEST && attr.data != null && data!!.getData() != null) {
            //this is the uri of the image use it to load the image 
            val imageUri: Uri = data!!.getData()
            Log.d(TAG, "onActivityResult: path:${imageUri.path}")
            viewModel.setEvent(Event.StartAnalysisAzure(imageUri.path))
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveScreenshot(view: View, saveBitmap: (Bitmap) -> Unit){
        val window = (view.context as Activity).window
        if (window != null) {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)
            try {
                PixelCopy.request(window, Rect(locationOfViewInWindow[0], locationOfViewInWindow[1], locationOfViewInWindow[0] + view.width, locationOfViewInWindow[1] + view.height), bitmap, { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        saveBitmap(bitmap)
                    }
                    // possible to handle other result codes ...
                }, Handler())
            } catch (e: IllegalArgumentException) {
                 // PixelCopy may throw IllegalArgumentException, make sure to handle it
            }
        }
    }
    fun getViewBitmap(v: View): Bitmap? {
        v.clearFocus()
        v.isPressed = false
        val willNotCache = v.willNotCacheDrawing()
        v.setWillNotCacheDrawing(false)
        val color = v.drawingCacheBackgroundColor
        v.drawingCacheBackgroundColor = 0
        if (color != 0) {
            v.destroyDrawingCache()
        }
        v.buildDrawingCache()
        val cacheBitmap = v.drawingCache ?: return null
        val bitmap = Bitmap.createBitmap(cacheBitmap)
        v.destroyDrawingCache()
        v.setWillNotCacheDrawing(willNotCache)
        v.drawingCacheBackgroundColor = color
        return bitmap
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