package com.example.edward.nyansapo.assess_process.paragraph

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ActivityParagraphAssessmentBinding
import com.example.edward.nyansapo.Assessment
import com.example.edward.nyansapo.presentation.ui.preassessment.PreAssessmentFragmentDirections
import com.example.edward.nyansapo.util.GlobalData
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.exhaustive
import com.example.edward.nyansapo.util.studentDocumentSnapshot
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@AndroidEntryPoint
class ParagraphAssessmentFragment : Fragment(R.layout.activity_paragraph_assessment) {
    private val TAG = "ParagraphAssessmentFrag"

    private lateinit var binding: ActivityParagraphAssessmentBinding
    private val navArgs: ParagraphAssessmentFragmentArgs by navArgs()
    private val viewModel: ParagraphAssessmentViewModel by viewModels()

    private val RC_PERMISSION = 4
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ActivityParagraphAssessmentBinding.bind(view)
        initProgressBar()
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
                viewModel.paragraphAssessmentEvents.collect {
                    when (it) {
                        is Event.PermissionGranted -> {
                            viewModel.setEvent(Event.FetchParagraph)
                            setOnClickListener()
                            animateArrow()

                        }
                        is Event.PermissionDenied -> {
                            requestPermission()
                        }

                        is Event.RecordClicked -> {
                            recordClicked()
                        }
                        is Event.StopRecording -> {
                            stopVoiceRecording()
                        }
                        is Event.GoToWord -> {
                            goToWord(it.assessment)
                        }
                        is Event.GoToStory -> {
                            goToStory(it.assessment)
                        }
                    }
                }
            }

            launch {
                viewModel.transcriptionsStatus.collect {
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)
                            startVoiceRecording()
                            viewModel.transcriptionStarted = true
                            activateTranscriptionColor()


                        }
                        Resource.Status.SUCCESS -> {
                            viewModel.transcriptionStarted = false
                            showProgress(false)
                            deactivateTranscriptionColor()
                        }
                        Resource.Status.ERROR -> {
                            viewModel.transcriptionStarted = false
                            deactivateTranscriptionColor()
                            showProgress(false)
                            Log.d(TAG, "subscribeToObservers: Error:${it.exception?.message}")
                            showToastInfo("Error:${it.exception?.message}")

                        }
                    }
                }
            }
            launch {
                viewModel.fetchParagraphStatus.collect {
                    Log.d(TAG, "subscribeToObservers: fetchParagraphStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.SUCCESS -> {
                            viewModel.setEvent(Event.FetchSentence)

                        }
                    }
                }
            }
            launch {
                viewModel.fetchSentenceStatus.collect {
                    Log.d(TAG, "subscribeToObservers: fetchParagraphStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.SUCCESS -> {
                            setSentence(it.data!!)

                        }
                    }
                }
            }
        }
    }

    private fun goToWord(assessment: Assessment) {

    }

    private fun goToStory(assessment: Assessment) {

    }


    private fun deactivateTranscriptionColor() {
        binding.paragraph1!!.background = originalReadButtonDrawable
        binding.paragraph1!!.setTextColor(Color.BLACK)

    }

    private fun stopVoiceRecording() {
        recorder.stop()
        recorder.release()


    }

    private fun setSentence(data: String) {
        binding.paragraph1!!.setText(data)
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private lateinit var originalReadButtonDrawable: Drawable
    private fun activateTranscriptionColor() {
        originalReadButtonDrawable = binding.paragraph1!!.background
        val newDrawable = originalReadButtonDrawable!!.getConstantState().newDrawable().mutate()
        val lightblue = Color.parseColor("#82b6ff") //light blue
        val lightbrown = Color.parseColor("#FFFF00") // bright yellow
        newDrawable.colorFilter = PorterDuffColorFilter(lightblue, PorterDuff.Mode.MULTIPLY)
        binding.paragraph1!!.background = newDrawable
        binding.paragraph1!!.setTextColor(lightbrown)


    }

    private fun recordClicked() {
        Log.d(TAG, "recordClicked: ")
        viewModel.setEvent(Event.InitTranscription)
    }


    lateinit var recorder: MediaRecorder
    lateinit var file: File
    private fun startVoiceRecording() {
        recorder = MediaRecorder()
        val status = Environment.getExternalStorageState();

        Log.d(TAG, "startVoiceRecording: sd card mounted")
        val timeStamp = Calendar.getInstance().time.time
        //    val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/nyansapo_recording/paragraphs/${studentDocumentSnapshot!!.id}/${assessment?.id}")
        //   directory.mkdirs()
        //   file = File(directory, "${sentence_count}.wav")
        file.createNewFile()

        Log.d(TAG, "startVoiceRecording: file path:${file.absolutePath}")
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(file.absolutePath)
        recorder.prepare()
        recorder.start()


    }

    private fun setOnClickListener() {
        /*    binding.skipButton.setOnClickListener {
                viewModel.setEvent(Event.SkipClicked)
            }*/

        /*      binding.readButton.setOnClickListener {
                  viewModel.setEvent(Event.RecordClicked)

              }*/

        binding.recordButton.setOnClickListener {
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
        binding.imageView4.setImageResource(navArgs.student.avatar)
    }

    private fun permissionIsGranted(): Boolean {
        val write_external_storage_result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val record_audio_result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED && record_audio_result == PackageManager.PERMISSION_GRANTED
    }


    private fun requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO), RC_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        when (requestCode) {
            RC_PERMISSION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    viewModel.setEvent(Event.SentPermission(true))
                else
                    Log.d(TAG, "onRequestPermissionsResult: permission denied")
            }
        }
    }


    sealed class Event {
        data class GoToWord(val assessment: Assessment) : Event()
        data class GoToStory(val assessment: Assessment) : Event()
        data class SentPermission(val granted: Boolean) : Event()
        object PermissionGranted : Event()
        object PermissionDenied : Event()
        object SkipClicked : Event()
        object RecordClicked : Event()
        object FetchParagraph : Event()
        object FetchSentence : Event()
        object StopRecording : Event()
        object InitTranscription : Event()
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