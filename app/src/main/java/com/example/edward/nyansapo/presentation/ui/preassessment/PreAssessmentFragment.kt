package com.example.edward.nyansapo.presentation.ui.preassessment

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.*
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
import com.edward.nyansapo.databinding.ActivityPreAssessmentBinding
import com.example.edward.nyansapo.Assessment
import com.example.edward.nyansapo.paragraph_chooser.ParagraphChooserActivity
import com.example.edward.nyansapo.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class PreAssessmentFragment : Fragment(R.layout.activity_pre_assessment) {

    private val TAG = "PreAssessmentFragment"

    // Permission
    private val REQUEST_PERSMISSION_CODE = 1000


    private lateinit var binding: ActivityPreAssessmentBinding
    private val viewModel: PreAssessmentViewModel by viewModels()
    private val navArgs: PreAssessmentFragmentArgs by navArgs()

    private lateinit var originalReadButtonDrawable: Drawable
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ActivityPreAssessmentBinding.bind(view)
        setDefaults()
        subscribeToObservers()
        if (permissionIsGranted()) {
            viewModel.setEvent(Event.SentPermission(true))
        } else {
            viewModel.setEvent(Event.SentPermission(false))
        }

    }

    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.preAssessmentEvents.collect {
                    when (it) {
                        is Event.PermissionGranted -> {
                            setOnClickListener()
                            animateArrow()

                        }
                        is Event.PermissionDenied -> {
                            requestPermission()
                        }
                        is Event.SkipClicked -> {
                            gotoParagraphChooser()
                        }
                        is Event.RecordClicked -> {
                            recordClicked()
                        }

                    }
                }
            }

            launch {
                viewModel.transcriptionsStatus.collect {
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)
                            viewModel.transcriptionStarted = true
                            initTranscriptionColor()


                        }
                        Resource.Status.SUCCESS -> {
                            viewModel.transcriptionStarted = false
                            showProgress(false)
                            gotoParagraphChooser()

                        }
                        Resource.Status.ERROR -> {
                            viewModel.transcriptionStarted = false

                            showProgress(false)
                            Log.d(TAG, "subscribeToObservers: Error:${it.exception?.message}")
                            showToastInfo("Error:${it.exception?.message}")

                        }
                    }
                }
            }
        }
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun initTranscriptionColor() {
        originalReadButtonDrawable = binding.readButton!!.background
        val newDrawable = originalReadButtonDrawable!!.getConstantState().newDrawable().mutate()
        val lightblue = Color.parseColor("#82b6ff") //light blue
        val lightbrown = Color.parseColor("#FFFF00") // bright yellow
        newDrawable.colorFilter = PorterDuffColorFilter(lightblue, PorterDuff.Mode.MULTIPLY)
        binding.readButton!!.background = newDrawable
        binding.readButton!!.setTextColor(lightbrown)


    }

    private fun recordClicked() {
        Log.d(TAG, "recordClicked: ")
        viewModel.setEvent(Event.StartTranscription)
    }

    private fun gotoParagraphChooser() {
        val uniqueID = Calendar.getInstance().timeInMillis.toString()
        val assessment = Assessment() // create new assessment object
        assessment.id = uniqueID
        assessment.assessmentKey = navArgs.assessmentKey // assign proper key
        findNavController().navigate(PreAssessmentFragmentDirections.actionPreAssessmentFragmentToParagraphChooserFragment(assessment,navArgs.student))
    }

    private fun setOnClickListener() {
        binding.skipButton.setOnClickListener {
            viewModel.setEvent(Event.SkipClicked)
        }
        binding.recordButton.setOnClickListener {
            viewModel.setEvent(Event.RecordClicked)

        }
        binding.readButton.setOnClickListener {
            viewModel.setEvent(Event.RecordClicked)

        }
    }

    private fun animateArrow() {
        val arrow_animation_leftToRight = AnimationUtils.loadAnimation(requireContext(), R.anim.lefttoright)
        arrow_animation_leftToRight!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                arrowBlink()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        binding.arrowImg!!.startAnimation(arrow_animation_leftToRight)

    }

    fun arrowBlink() {
        val arrow_animation_blink = AnimationUtils.loadAnimation(requireContext(), R.anim.blink_anim)
        binding.arrowImg!!.startAnimation(arrow_animation_blink)
        arrow_animation_blink!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                arrowFadeOut()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    fun arrowFadeOut() {
        val arrow_animation_fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fadeout)
        binding.arrowImg!!.startAnimation(arrow_animation_fadeOut)

    }

    private fun setDefaults() {
        binding.imageView3.setImageResource(navArgs.student.avatar)
    }

    private fun permissionIsGranted(): Boolean {
        val write_external_storage_result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val record_audio_result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED && record_audio_result == PackageManager.PERMISSION_GRANTED
    }


    private fun requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO), REQUEST_PERSMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        when (requestCode) {
            REQUEST_PERSMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    viewModel.setEvent(Event.SentPermission(true))
                else
                    Log.d(TAG, "onRequestPermissionsResult: permission denied")
            }
        }
    }


    sealed class Event {
        data class SentPermission(val granted: Boolean) : Event()
        object PermissionGranted : Event()
        object PermissionDenied : Event()
        object SkipClicked : Event()
        object RecordClicked : Event()
        object StartTranscription : Event()
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