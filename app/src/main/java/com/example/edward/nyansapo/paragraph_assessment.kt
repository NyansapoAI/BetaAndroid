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
import com.edward.nyansapo.R
import com.example.edward.nyansapo.presentation.utils.GlobalData
import com.example.edward.nyansapo.presentation.utils.studentDocumentSnapshot
import com.microsoft.cognitiveservices.speech.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_paragraph_assessment.*
import kotlinx.android.synthetic.main.activity_pre_assessment.*
import java.io.File
import java.util.*
import java.util.concurrent.ExecutionException

@AndroidEntryPoint
class paragraph_assessment : AppCompatActivity() {

    private val TAG = "paragraph_assessment"


    private val RC_PERMISSION = 4
    lateinit var file: File
    lateinit var recorder: MediaRecorder


    lateinit var skipBtn: Button
    var paragraphButton: Button? = null
    var record_button: Button? = null
    var paragraph: String? = null
    lateinit var sentences: Array<String>
    var error_count = 0
    var sentence_count = 0
    var tries = 0

    // assessment content
    var assessment_content: Assessment_Content? = null
    var assessment: Assessment? = null
    var ASSESSMENT_KEY: String? = null

    // paragraph
    var paragraph_words_wrong = ""


    /// Control variables or code locks

    var transcriptStarted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paragraph_assessment)
        skipBtn = findViewById(R.id.skipBtn)
        skipBtn.setOnClickListener {
            skipBtnPressed()
        }
        initProgressBar()
        //setting choosen avatar
        imageView4.setImageResource(GlobalData.avatar)
        val intent = intent

        //Toast.makeText(this, "Click on the Record Button to read or click on change to change the prompt", Toast.LENGTH_LONG).show();
        assessment = intent.getParcelableExtra("Assessment")
        ASSESSMENT_KEY = assessment!!.assessmentKey
        //Toast.makeText(this,instructor_id, Toast.LENGTH_LONG ).show();
        assessment_content = Assessment_Content()
        val para = getPara(ASSESSMENT_KEY)
        paragraph = para[0]
        //Toast.makeText(this, intent.getStringExtra("paragraph"), Toast.LENGTH_LONG);
        paragraph = if (intent.getStringExtra("paragraph").equals("0", ignoreCase = true)) {

            assessment!!.paragraphChoosen = 0
            para[0]
        } else {
            assessment!!.paragraphChoosen=1
            para[1]
        }


        sentences = paragraph!!.split(".").map {
            it.trim()
        }.filter { line ->
            line.isNotBlank()
        }.toTypedArray()

        paragraphButton = findViewById(R.id.paragraph1)


        //mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.paragraph);
        //mediaPlayer.start();

        //changeButton.setEnabled(false);

        paragraphButton!!.setText(sentences[0])

        record_button = findViewById(R.id.record_button)
        record_button!!.setOnClickListener {

            checkIfWeHavePermissions()
            //    recordStudent()
        }
        paragraphButton!!.setOnClickListener {
            checkIfWeHavePermissions()

            //    recordStudent()
        }
    }

    private fun skipBtnPressed() {

        val expected_txt = paragraphButton!!.getText().toString().toLowerCase().replace(".", "")!!.replace(",", "")

        val expectedTextListDummy = expected_txt.split(" ").filter {
            it.isNotBlank()
        }.map {
            it.trim()
        }

        var error_txt = ""
        expectedTextListDummy.forEach {
            error_txt += it + ","
            Log.d(TAG, "onPostExecute: error_text:$error_txt")

        }
        error_count += expectedTextListDummy.size
        paragraph_words_wrong += error_txt.trim()
        changeSentence()
    }

    var drawable: Drawable? = null


    fun recordStudent() {
        if (!transcriptStarted) {
            drawable = paragraphButton!!.background
            val newDrawable = drawable!!.getConstantState().newDrawable().mutate()
            val lightblue = Color.parseColor("#8B4513")

            val lightbrown = Color.parseColor("#FFFF00") // bright yellow


            newDrawable.colorFilter = PorterDuffColorFilter(lightblue, PorterDuff.Mode.MULTIPLY)
            paragraphButton!!.background = newDrawable
            paragraphButton!!.setTextColor(lightbrown)
            SpeechAsync().execute()
            transcriptStarted = true
        }

    }

    fun changeSentence() {

        Log.d(TAG, "changeSentence: ")
        Log.d(TAG, "changeSentence: paragraph_words_wrong:$paragraph_words_wrong")
        Log.d(TAG, "changeSentence: error_count:$error_count")
        stopVoiceRecording()
        Log.d(TAG, "changeSentence: assessmentRecording:${GlobalData.assessmentRecording}")


        tries = 0 // everytime a sentence is changed tries go to one
        if (sentence_count < sentences.size - 1) {
            sentence_count += 1 // increment sentence count
            paragraphButton!!.text = sentences[sentence_count]
            Log.d(TAG, "changeSentence: sentence_count:$sentence_count sentence_size: ${sentences.size - 1}")

        } else { // move to another  level //only if we have finished reading all the paragraph containing 4 sentences
            if (error_count > 2) {
                goToWord()
            } else {
                goToStory()
            }
        }
    }

    private fun stopVoiceRecording() {
        Log.d(TAG, "stopRecordingVoice: sentence_count:$sentence_count")
        if (this::recorder.isInitialized) {
            try {
                recorder.stop()
                recorder.release()

            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }


    }



    inner class SpeechAsync : AsyncTask<Void, String?, String?>() {
        var speechSubscriptionKey = "1c58abdab5d74d5fa41ec8b0b4a62367"

        var serviceRegion = "eastus"
        var endpoint = "275310be-2c21-4131-9609-22733b4e0c04"
        var config: SpeechConfig? = null
        var reco: SpeechRecognizer? = null

        lateinit var expected_txt: String
        override fun onPreExecute() {
            super.onPreExecute()

            startVoiceRecording()
            config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion)
            config!!.setEndpointId(endpoint)
            paragraphButton = findViewById(R.id.paragraph1)
            expected_txt = paragraphButton!!.getText().toString().toLowerCase().replace(".", "")!!.replace(",", "")

        }


        override fun onCancelled(s: String?) {
            super.onCancelled(s)
            reco!!.close()
        }

        override fun onPostExecute(textFromServer: String?) {
            super.onPostExecute(textFromServer)
            Log.d(TAG, "onPostExecute: received textFromServer: $textFromServer")
            paragraphButton!!.background = drawable
            paragraphButton!!.setTextColor(Color.BLACK)


            transcriptStarted = false


            if (textFromServer.equals("canceled", ignoreCase = true)) {
                Toast.makeText(this@paragraph_assessment, "Internet Connection Failed", Toast.LENGTH_LONG).show()
            } else if (textFromServer.equals("no match", ignoreCase = true)) {
                Toast.makeText(this@paragraph_assessment, "Try Again", Toast.LENGTH_LONG).show()
            } else {
                /////////////////////////////////////////

                var textFromServerFormatted = textFromServer?.replace(".", "")!!.replace(",", "")!!
                Log.d(TAG, "onPostExecute:removed dot and comma textFromServerFormatted : $textFromServerFormatted")

                var listOfTxtFromServer = textFromServerFormatted.split(" ").map {
                    it.trim()
                }.filter {
                    it.isNotBlank()
                }


                Log.d(TAG, "onPostExecute: textFromServer Split to list listOfTxtFromServer: $listOfTxtFromServer")


                val expectedTextListDummy = expected_txt.split(" ").filter {
                    it.isNotBlank()
                }.map {
                    it.trim()
                }

                Log.d(TAG, "onPostExecute: list of words expected expectedTextListDummy: $expectedTextListDummy")
                (expectedTextListDummy as ArrayList).removeAll(listOfTxtFromServer)
                val countErrorFromSentence = expectedTextListDummy.size
                Log.d(TAG, "onPostExecute: expectedTextListDummy $expectedTextListDummy")
                Log.d(TAG, "onPostExecute: number of words got wrong: $countErrorFromSentence")
                Log.d(TAG, "onPostExecute: words got wrong are:$expectedTextListDummy")

                var error_txt = ""
                expectedTextListDummy.forEach {
                    error_txt += it + ","
                    Log.d(TAG, "onPostExecute: error_text:$error_txt")

                }


                if (countErrorFromSentence > 2 || listOfTxtFromServer.size < 2) {
                    Log.d(TAG, "onPostExecute: number of words got wrong: $countErrorFromSentence and number of words retrieved from server ${listOfTxtFromServer.size}")
                    if (tries < 1) {
                        tries++
                        Toast.makeText(paragraphButton!!.context, "Try Again!", Toast.LENGTH_LONG).show()
                    } else {
                        error_count += countErrorFromSentence
                        if (countErrorFromSentence != 0) { // if no erro
                            //words_correct += ","+expected_txt.trim();
                            paragraph_words_wrong += error_txt.trim()
                            Log.d(TAG, "onPostExecute: paragraph_words_wrong: $paragraph_words_wrong ")
                        }


                        changeSentence()
                    }
                } else {
                    error_count += countErrorFromSentence
                    Log.d(TAG, "onPostExecute: error_count: $error_count : countErrorFromSentence: $countErrorFromSentence")

                    if (countErrorFromSentence != 0) { // if no erro
                        //words_correct += ","+expected_txt.trim();
                        paragraph_words_wrong += error_txt.trim()
                        Log.d(TAG, "onPostExecute: paragraph_words_wrong: $paragraph_words_wrong")
                    }
                    Log.d(TAG, "onPostExecute: paragraph_words_wrong: $paragraph_words_wrong")

                    changeSentence()
                }
            }
            reco!!.close()
        }

        override fun doInBackground(vararg void: Void): String? {
            try {
                reco = SpeechRecognizer(config)
                var result: SpeechRecognitionResult? = null
                val task = reco!!.recognizeOnceAsync()!!

                // Note: this will block the UI thread, so eventually, you want to
                //        register for the event (see full samples)
                try {
                    result = task.get()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                assert(result != null)
                if (result!!.reason == ResultReason.RecognizedSpeech) {
                    return result.text.toLowerCase()
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

    private fun checkIfWeHavePermissions() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.RECORD_AUDIO), RC_PERMISSION)
            Log.d(TAG, "checkIfWeHavePermissions: permission not available")
        } else {
            Log.d(TAG, "checkIfWeHavePermissions: permissions available")
            recordStudent()
        }
    }


    private fun startVoiceRecording() {
        Log.d(TAG, "startVoiceRecording: assessment:${assessment}")



   
        recorder = MediaRecorder()
        val status = Environment.getExternalStorageState();

            Log.d(TAG, "startVoiceRecording: sd card mounted")
            val timeStamp = Calendar.getInstance().time.time
            val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/nyansapo_recording/paragraphs/${studentDocumentSnapshot!!.id}/${assessment?.id}")
            directory.mkdirs()
            file = File(directory, "${sentence_count}.wav")
            file.createNewFile()

            Log.d(TAG, "startVoiceRecording: file path:${file.absolutePath}")
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(file.absolutePath)
            recorder.prepare()
            recorder.start()


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RC_PERMISSION && grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onRequestPermissionsResult: permission granted")

            recordStudent()
        } else {
            Log.d(TAG, "onRequestPermissionsResult: permission denied")
        }


    }

    fun goToStory() {
        assessment!!.paragraphWordsWrong = paragraph_words_wrong // set words wrong
        val myIntent = Intent(baseContext, story_assessment::class.java)
        myIntent.putExtra("Assessment", assessment)
        startActivity(myIntent)
        finish()

        /* val map = mapOf("paragraphWordsWrong" to paragraph_words_wrong)
         showProgress(true)
         assessmentDocumentSnapshot!!.reference.set(map, SetOptions.merge()).addOnSuccessListener {
             showProgress(false)


         }*/

    }

    fun goToWord() {

        assessment!!.paragraphWordsWrong = paragraph_words_wrong
        val myIntent = Intent(baseContext, word_assessment::class.java)
        myIntent.putExtra("Assessment", assessment)
        startActivity(myIntent)
        finish()

    }

    fun getPara(key: String?): Array<String> {
        return when (key) {
            "3" -> {
                Assessment_Content.getP3()
            }
            "4" -> {
                Assessment_Content.getP4()
            }
            "5" -> {
                Assessment_Content.getP5()
            }
            "6" -> {
                Assessment_Content.getP6()
            }
            "7" -> {
                Assessment_Content.getP7()
            }
            "8" -> {
                Assessment_Content.getP8()
            }
            "9" -> {
                Assessment_Content.getP9()
            }
            "10" -> {
                Assessment_Content.getP10()
            }
            else -> Assessment_Content.getP3()
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