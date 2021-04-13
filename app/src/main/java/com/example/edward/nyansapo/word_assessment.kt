package com.example.edward.nyansapo

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.media.MediaRecorder
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.view.ViewGroup.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.edward.nyansapo.presentation.utils.GlobalData
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import kotlinx.android.synthetic.main.activity_pre_assessment.*
import kotlinx.android.synthetic.main.activity_word_assessment.*
import java.io.File
import java.util.*
import java.util.concurrent.ExecutionException
import com.edward.nyansapo.R
import com.example.edward.nyansapo.presentation.utils.studentDocumentSnapshot


class word_assessment : AppCompatActivity() {

    private val TAG = "word_assessment"
    private val RC_PERMISSION = 7


    lateinit var file: File
    lateinit var recorder: MediaRecorder


    lateinit var wordList: Array<String>
    var error_count = 0
    var word_count = 0
    var words_tried = 0
    // assessment content
    var assessment_content: Assessment_Content? = null
    var assessment: Assessment? = null
    lateinit var ASSESSMENT_KEY: String

    // words wrong
    var words_wrong = ""
    var words_correct = ""

    // UI
    var record_button: Button? = null
    var assessment_card: Button? = null
    var change_button: Button? = null


    var transcriptStarted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_assessment)
        initProgressBar()
        //setting choosen avatar
        imageView4.setImageResource(GlobalData.avatar)
        Toast.makeText(this, "Click on the Record Button to read or click on change to change the prompt", Toast.LENGTH_LONG).show()

        // will replace later
        val intent = intent
        //Toast.makeText(this,instructor_id, Toast.LENGTH_LONG ).show();
        assessment = intent.getParcelableExtra("Assessment")
        ASSESSMENT_KEY = assessment!!.assessmentKey
        assessment_content = Assessment_Content()
        wordList = getWords(ASSESSMENT_KEY).map { it.trim().toLowerCase() }.toTypedArray()


        //ui components
        record_button = findViewById(R.id.record_button)
        change_button = findViewById(R.id.change_button)
        assessment_card = findViewById(R.id.assessment_card)


        // intialize
        error_count = 0
        word_count = 0
        words_tried = 0

        // assign first word
        assessment_card!!.text = wordList[0]

        // on click listeners
        assessment_card!!.setOnClickListener { checkIfWeHavePermissions() }

        record_button!!.setOnClickListener { checkIfWeHavePermissions() }
    }

    private fun checkIfWeHavePermissions() {
        Log.d(TAG, "checkIfWeHavePermissions: ")

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.RECORD_AUDIO), RC_PERMISSION)
            Log.d(TAG, "checkIfWeHavePermissions: permission not available")
        } else {
            Log.d(TAG, "checkIfWeHavePermissions: permissions available")
            recordStudent()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: ")

        if (requestCode == RC_PERMISSION && grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onRequestPermissionsResult: permission granted")

            recordStudent()
        } else {
            Log.d(TAG, "onRequestPermissionsResult: permission denied")
        }


    }


    var drawable: Drawable? = null
    fun recordStudent() {
        if (!transcriptStarted) {
            drawable = assessment_card!!.background
            val newDrawable = drawable!!.constantState.newDrawable().mutate()
            val lightblue = Color.parseColor("#8B4513")

            val lightbrown = Color.parseColor("#FFFF00") // bright yellow


            newDrawable.colorFilter = PorterDuffColorFilter(lightblue, PorterDuff.Mode.MULTIPLY)
            assessment_card!!.background = newDrawable
            assessment_card!!.setTextColor(lightbrown)
            SpeechAsync().execute()
            transcriptStarted = true
        }
    }

    private fun stopVoiceRecording() {
        Log.d(TAG, "stopRecordingVoice: word_count:$word_count")
        recorder.stop()
        recorder.release()


        when (word_count) {
            0 -> {
                Log.d(TAG, "stopRecordingVoice:chooser word_count:0")

                GlobalData.assessmentRecording.word0 = file.absolutePath
            }
            1 -> {
                Log.d(TAG, "stopRecordingVoice:chooser word_count:1")

                GlobalData.assessmentRecording.word1 = file.absolutePath
            }
            2 -> {
                Log.d(TAG, "stopRecordingVoice:chooser word_count:2")

                GlobalData.assessmentRecording.word2 = file.absolutePath
            }
            3 -> {
                Log.d(TAG, "stopRecordingVoice:chooser word_count:3")

                GlobalData.assessmentRecording.word3 = file.absolutePath
            }
            4 -> {
                Log.d(TAG, "stopRecordingVoice:chooser word_count:4")

                GlobalData.assessmentRecording.word4 = file.absolutePath
            }
            5 -> {
                Log.d(TAG, "stopRecordingVoice:chooser word_count:5")

                GlobalData.assessmentRecording.word5 = file.absolutePath
            }
        }


    }

    fun changeWord() {
        Log.d(TAG, "changeWord: ")
        Log.d(TAG, "changeWord: error_count:$error_count")

        stopVoiceRecording()
        Log.d(TAG, "changeWord: assessmentRecording:${GlobalData.assessmentRecording}")


        if (words_tried > 4) { // if 6 has been tried
            Log.d(TAG, "changeWord: words tried:$words_tried")
            Log.d(TAG, "changeWord: error_count:$error_count")
            if (error_count < 2) {
                Log.d(TAG, "changeWord:go to thank you error_count:$error_count less than 2")
                Log.d(TAG, "changeWord: words_correct:$words_correct")
                Log.d(TAG, "changeWord: words_wrong:$words_wrong")

                goToThankYou()
            } else {
                Log.d(TAG, "changeWord:go to letter  error_count:$error_count more than 2")
                Log.d(TAG, "changeWord: words_correct:$words_correct")
                Log.d(TAG, "changeWord: words_wrong:$words_wrong")
                goToLetter()
            }
        } else if (word_count < wordList.size - 1) {
            word_count += 1 // increment sentence count
            words_tried += 1
            assessment_card!!.text = wordList[word_count]

            Log.d(TAG, "changeWord: words_correct:$words_correct")
            Log.d(TAG, "changeWord: words_wrong:$words_wrong")

            Log.d(TAG, "changeWord: word_count:$word_count number of words:${wordList.size - 1}")
        } else {
            word_count = 0 // dont know why
        }
    }

    private fun startVoiceRecording() {
        Log.d(TAG, "startVoiceRecording: Environment.getexternalStorageDirectory():${Environment.getExternalStorageDirectory()}")
        recorder = MediaRecorder()
        val status = Environment.getExternalStorageState();
        if (status.equals("mounted")) {
            Log.d(TAG, "startVoiceRecording: sd card mounted")
            val timeStamp = Calendar.getInstance().time.time
            val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/nyansapo_recording/words/${studentDocumentSnapshot!!.id}/${assessment?.id}")
            directory.mkdirs()
            file = File(directory, "${wordList[word_count]}.wav")
            file.createNewFile()

            Log.d(TAG, "startVoiceRecording: file path:${file.absolutePath}")
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(file.absolutePath)
            recorder.prepare()
            recorder.start()
        } else {
            Log.d(TAG, "startVoiceRecording: sd card not mounted")

        }

    }


    inner class SpeechAsync : AsyncTask<Void, String?, String?>() {
        // Replace below with your own subscription key
        var speechSubscriptionKey = "1c58abdab5d74d5fa41ec8b0b4a62367"

        // Replace with your own subscription key and region identifier from here: https://aka.ms/speech/sdkregion
        var serviceRegion = "eastus"
        var endpoint = "275310be-2c21-4131-9609-22733b4e0c04"
        var config: SpeechConfig? = null
        var reco: SpeechRecognizer? = null
        var view: View? = null
        var expected_txt: String? = null
        override fun onPreExecute() {
            super.onPreExecute()
            Log.d(TAG, "onPreExecute: ")
            startVoiceRecording()

            config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion)
            config!!.endpointId = endpoint
            assessment_card = findViewById(R.id.assessment_card)
            expected_txt = assessment_card!!.text.toString().trim().toLowerCase()
            //assessment_card.setEnabled(false);
            //record_button.setEnabled(false);
        }


        override fun onCancelled(s: String?) {
            super.onCancelled(s)
            reco!!.close()
        }

        override fun onPostExecute(textFromServer: String?) {
            super.onPostExecute(textFromServer)
            Log.d(TAG, "onPostExecute: textFromServer:$textFromServer")

            assessment_card!!.background = drawable
            assessment_card!!.setTextColor(Color.BLACK)
            transcriptStarted = false

            if (textFromServer.equals("canceled", ignoreCase = true)) {
                Toast.makeText(this@word_assessment, "Internet Connection Failed", Toast.LENGTH_LONG).show()
            } else if (textFromServer.equals("no match", ignoreCase = true)) {
                Toast.makeText(this@word_assessment, "Try Again Please", Toast.LENGTH_LONG).show()
            } else {
                var textFromServerFormatted = textFromServer!!.replace(".", "")

                Log.d(TAG, "onPostExecute: remove dots textFromServerFormatted :$textFromServerFormatted")

                if (expected_txt.equals(textFromServerFormatted)) {
                    Log.d(TAG, "onPostExecute: word is correct expected: $expected_txt text from server: $textFromServerFormatted")
                    words_correct += expected_txt!! + ","
                    Log.d(TAG, "onPostExecute: correct words: $words_correct ")
                    Log.d(TAG, "onPostExecute: wrong words: $words_wrong")

                    Log.d(TAG, "onPostExecute: error_count: $error_count")


                } else {
                    words_wrong += expected_txt!! + ","
                    Log.d(TAG, "onPostExecute: word is wrong expected_text: $expected_txt :textFromServerFormatted: $textFromServerFormatted")
                    Log.d(TAG, "onPostExecute: wrong words: $words_wrong")
                    Log.d(TAG, "onPostExecute: correct words: $words_correct ")
                    error_count += 1

                    Log.d(TAG, "onPostExecute: error_count: $error_count")
                }
                changeWord()
            }
            reco!!.close()
        }

        override fun doInBackground(vararg p0: Void): String? {
            try {
                reco = SpeechRecognizer(config)
                var result: SpeechRecognitionResult? = null
                val task = reco!!.recognizeOnceAsync()!!


                try {
                    result = task.get()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                assert(result != null)
                if (result!!.reason == ResultReason.RecognizedSpeech) {
                    return result.text.toLowerCase().trim()
                } else if (result.reason == ResultReason.NoMatch) {
                    return "no match"
                } else if (result.reason == ResultReason.Canceled) {
                    return "canceled"
                }
            } catch (err: Error) {
                return " Error" + err.message.toString()
            }
            return null
        }


    }


    fun goToLetter() {
        val myIntent = Intent(baseContext, letter_assessment::class.java)
        assessment!!.wordsWrong = words_wrong
        assessment!!.wordsCorrect = words_correct
        myIntent.putExtra("Assessment", assessment)
        startActivity(myIntent)
        finish()
/*        val map = mapOf("wordsWrong" to words_wrong, "wordsCorrect" to words_correct)
        showProgress(true)
        assessmentDocumentSnapshot!!.reference.set(map, SetOptions.merge()).addOnSuccessListener {
            showProgress(false)




        }*/

    }

    fun goToThankYou() {
        val myIntent = Intent(baseContext, thankYou::class.java)
        assessment!!.wordsWrong = words_wrong
        assessment!!.wordsCorrect = words_correct
        assessment!!.learningLevel = "WORD"
        myIntent.putExtra("Assessment", assessment)
        startActivity(myIntent)
        finish()


        /*showProgress(true)
        val map = mapOf("wordsWrong" to words_wrong, "wordsCorrect" to words_correct, "learningLevel" to "WORD")

        assessmentDocumentSnapshot!!.reference.set(map, SetOptions.merge()).addOnSuccessListener {

            //updating student learning level
            val map2 = mapOf("learningLevel" to "WORD")
            studentDocumentSnapshot!!.reference.set(map2, SetOptions.merge()).addOnSuccessListener {
                showProgress(false)


            }

        }*/

    }

    fun getWords(key: String?): Array<String> {
        return when (key) {
            "3" -> {
                Assessment_Content.getW3()
            }
            "4" -> {
                Assessment_Content.getW4()
            }
            "5" -> {
                Assessment_Content.getW5()
            }
            "6" -> {
                Assessment_Content.getW6()
            }
            "7" -> {
                Assessment_Content.getW7()
            }
            "8" -> {
                Assessment_Content.getW8()
            }
            "9" -> {
                Assessment_Content.getW9()
            }
            "10" -> {
                Assessment_Content.getW10()
            }
            else -> Assessment_Content.getW3()
        }
    }

    companion object {
        //audio stuff
        private var mEMA = 0.0
        private const val EMA_FILTER = 0.6
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

        dialog = setProgressDialog(this, "Loading..")
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