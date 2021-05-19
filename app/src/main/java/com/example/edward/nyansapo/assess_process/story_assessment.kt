package com.example.edward.nyansapo.assess_process


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
import com.example.edward.nyansapo.util.GlobalData
import com.example.edward.nyansapo.util.studentDocumentSnapshot
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import kotlinx.android.synthetic.main.activity_story_assessment.*
import java.io.File
import java.util.*
import java.util.concurrent.ExecutionException
import kotlin.collections.ArrayList
import com.edward.nyansapo.R
import com.example.edward.nyansapo.Assessment
import com.example.edward.nyansapo.Assessment_Content
import com.example.edward.nyansapo.storyQuestions
import com.example.edward.nyansapo.thankYou

class story_assessment : AppCompatActivity() {

    private val TAG = "story_assessment"
    private val RC_PERMISSION = 4
    lateinit var file: File
    lateinit var recorder: MediaRecorder

    var next_button: Button? = null
    var story_view: Button? = null
    var back_button: Button? = null

    //TextView story_view;
    var storyString: String? = null
    lateinit var sentenceList: Array<String>
    var sentence_count = 0

    // assessment content
    var assessment_content: Assessment_Content? = null
    var assessment: Assessment? = null
    var ASSESSMENT_KEY: String? = null

    // story reading evaluation
    var story_words_wrong = ""
    var error_count = 0
    var tries = 0


    var transcriptStarted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_assessment)
        initProgressBar()
        //setting choosen avatar
        imageView5.setImageResource(GlobalData.avatar)

        assessment = intent.getParcelableExtra("Assessment")

        ASSESSMENT_KEY = assessment!!.assessmentKey

        assessment_content = Assessment_Content()

        storyString = getStory(ASSESSMENT_KEY)
        back_button = findViewById(R.id.back_button)
        next_button = findViewById(R.id.next_button)
        story_view = findViewById(R.id.story_view)


        //story = "One day the wind chased the sun away. It told the sun to go to another sky. The sun did not go. The next morning, the wind ran after the sun. The sun fell down and started crying. That is how it began to rain. We clapped for Juma.\\n\\n One day the wind chased the sun away. It told the sun to go to another sky. The sun did not go. The next morning, the wind ran after the sun. The sun fell down and started crying. That is how it began to rain. We clapped for Juma.";
        sentenceList = storyString!!.split(".")
                .filter { line ->
                    line.isNotBlank()
                }.map {
                    it.trim()
                }.toTypedArray()

        sentence_count = 0
        tries = 0
        error_count = 0

        story_view!!.setText(sentenceList[sentence_count])
        back_button!!.setEnabled(false)
        next_button!!.setOnClickListener {
            nextParagraph()
        }
        back_button!!.setOnClickListener { backParagraph() }
        story_view!!.setOnClickListener { checkIfWeHavePermissions() }


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
        Log.d(TAG, "startVoiceRecording: Environment.getexternalStorageDirectory():${Environment.getExternalStorageDirectory()}")
        recorder = MediaRecorder()
        val status = Environment.getExternalStorageState();
        if (status.equals("mounted")) {
            Log.d(TAG, "startVoiceRecording: sd card mounted")
            val timeStamp = Calendar.getInstance().time.time

           // val student= studentDocumentSnapshot!!.toObject(Student::class.java)
            val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/nyansapo_recording/storys/${studentDocumentSnapshot!!.id}/${assessment?.id}")
            directory.mkdirs()
            file = File(directory, "$sentence_count.wav")
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

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
            drawable = story_view!!.background
            val newDrawable = drawable!!.getConstantState().newDrawable().mutate()

            val lightblue = Color.parseColor("#82b6ff") //light blue
            val lightbrown = Color.parseColor("#eecab1") // light brown

            newDrawable.colorFilter = PorterDuffColorFilter(lightblue, PorterDuff.Mode.MULTIPLY)

            story_view!!.background = newDrawable
            story_view!!.setTextColor(lightbrown)
            SpeechAsync().execute()
            transcriptStarted = true
        }

    }

    fun backParagraph() {
        Log.d(TAG, "backParagraph: back button clicked")
        if (sentence_count == 0) {
            back_button!!.isEnabled = false
        } else {
            sentence_count--
            story_view!!.text = sentenceList[sentence_count]
            Log.d(TAG, "backParagraph: back button clicked current sentence: $sentence_count :size :${sentenceList.size - 1}")

            if (sentence_count == 0) {
                back_button!!.isEnabled = false
            }
        }
    }

    fun nextParagraph() {
        Log.d(TAG, "nextParagraph: ")
        Log.d(TAG, "nextParagraph: story_words_wrong:$story_words_wrong")
        Log.d(TAG, "nextParagraph: error_count:$error_count")
        stopVoiceRecording()
        Log.d(TAG, "changeSentence: assessmentRecording:${GlobalData.assessmentRecording}")


        tries = 0
        if (sentence_count < sentenceList.size - 1) {
            sentence_count += 1
            story_view!!.text = sentenceList[sentence_count]
            Log.d(TAG, "nextParagraph: current sentence:$sentence_count: :size:${sentenceList.size - 1}")
        } else {
            Log.d(TAG, "nextParagraph: current sentence:$sentence_count: :size:${sentenceList.size - 1}")
            Log.d(TAG, "nextParagraph: going to answer questions screen")
            assessment!!.storyWordsWrong = story_words_wrong
            val myIntent = Intent(baseContext, storyQuestions::class.java)
            myIntent.putExtra("Assessment", assessment)
            myIntent.putExtra("question", "0")
            startActivity(myIntent)

        }
    }

    private fun stopVoiceRecording() {
        Log.d(TAG, "stopRecordingVoice: sentence_count:$sentence_count")
        recorder.stop()
        recorder.release()

        when (sentence_count) {
            0 -> {
                Log.d(TAG, "stopRecordingVoice:chooser sentence_count:0")

                GlobalData.assessmentRecording.paragraph0 = file.absolutePath
            }
            1 -> {
                Log.d(TAG, "stopRecordingVoice:chooser sentence_count:1")

                GlobalData.assessmentRecording.paragraph1 = file.absolutePath
            }
            2 -> {
                Log.d(TAG, "stopRecordingVoice:chooser sentence_count:2")

                GlobalData.assessmentRecording.paragraph2 = file.absolutePath
            }
            3 -> {
                Log.d(TAG, "stopRecordingVoice:chooser sentence_count:3")

                GlobalData.assessmentRecording.paragraph3 = file.absolutePath
            }
        }


    }

    private inner class SpeechAsync : AsyncTask<Void, String?, String?>() {
        // Replace below with your own subscription key
        var speechSubscriptionKey = "1c58abdab5d74d5fa41ec8b0b4a62367"

        // Replace with your own subscription key and region identifier from here: https://aka.ms/speech/sdkregion
        var serviceRegion = "eastus"
        var endpoint = "275310be-2c21-4131-9609-22733b4e0c04"
        var config: SpeechConfig? = null
        var reco: SpeechRecognizer? = null
        var expected_txt: String? = null
        override fun onPreExecute() {
            super.onPreExecute()
            startVoiceRecording()

            config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion)
            config!!.setEndpointId(endpoint)
            story_view = findViewById(R.id.story_view)
            expected_txt = story_view!!.getText().toString().toLowerCase().trim().replace(".", "")!!.replace(",", "")
        }


        override fun onCancelled(s: String?) {
            super.onCancelled(s)
            reco!!.close()
        }

        override fun onPostExecute(textFromServer: String?) {
            super.onPostExecute(textFromServer)
            Log.d(TAG, "onPostExecute: textFromServer:$textFromServer")

            story_view!!.background = drawable
            story_view!!.setTextColor(Color.BLACK)

            transcriptStarted = false

            if (textFromServer.equals("canceled", ignoreCase = true)) {
                Toast.makeText(this@story_assessment, "Internet Connection Failed", Toast.LENGTH_LONG).show()
            } else if (textFromServer.equals("no match", ignoreCase = true)) {
                Toast.makeText(this@story_assessment, "Try Again", Toast.LENGTH_LONG).show()
            } else {


                var textFromServerFormatted = textFromServer?.replace(".", "")!!.replace(",", "")
                Log.d(TAG, "onPostExecute:removed dot and commas textFromServerFormatted : $textFromServerFormatted")

                var listOfTxtFromServer = textFromServerFormatted.split(" ").map {
                    it.trim()
                }.filter {
                    it.isNotBlank()
                }


                Log.d(TAG, "onPostExecute: textFromServer Split to list listOfTxtFromServer: $listOfTxtFromServer")


                val expectedTextListDummy = expected_txt!!.split(" ").filter {
                    it.isNotBlank()
                }.map {
                    it.trim()
                }
                Log.d(TAG, "onPostExecute: list of words expected expectedTextListDummy: $expectedTextListDummy")
                (expectedTextListDummy as ArrayList).removeAll(listOfTxtFromServer)
                val countErrorFromSentence = expectedTextListDummy.size
                Log.d(TAG, "onPostExecute: words got wrong expectedTextListDummy: $expectedTextListDummy")
                Log.d(TAG, "onPostExecute: number of words got wrong: $countErrorFromSentence")
                var error_txt = ""
                expectedTextListDummy.forEach {
                    error_txt += it + ","
                    Log.d(TAG, "onPostExecute: error_text:$error_txt")

                }

                val dummy_error_count = error_count + countErrorFromSentence

                if (dummy_error_count > 10) { // if error less than 8 move to story level
                    story_words_wrong += error_txt
                    Log.d(TAG, "onPostExecute: error_count is greater than 10 :dummy_error_count:$dummy_error_count")
                    goToThankYou()
                } else {
                    Log.d(TAG, "onPostExecute: error_count is 10 or lesser:dummy_error_count:$dummy_error_count")
                    if (countErrorFromSentence > 3 || listOfTxtFromServer.size < 2) {
                        if (tries < 1) {
                            tries++ // incremnent tries
                            Toast.makeText(story_view!!.context, "Try Again!", Toast.LENGTH_LONG).show()
                        } else {

                            if (countErrorFromSentence != 0) {
                                story_words_wrong += error_txt
                                error_count += countErrorFromSentence

                            }
                            nextParagraph()
                        }
                    } else {
                        if (countErrorFromSentence != 0) { //there is an error
                            story_words_wrong += error_txt
                            error_count += countErrorFromSentence

                        }
                        //    error_count += countErrorFromSentence
                        nextParagraph()
                    }
                }
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


    fun goToThankYou() { // take to thank you page and grade as paragraph student_activity
        val myIntent = Intent(baseContext, thankYou::class.java)
        assessment!!.learningLevel = "PARAGRAPH"
        assessment!!.storyWordsWrong = story_words_wrong
        //assessment.setSTORY_WORDS_WRONG(story_words_wrong);
        myIntent.putExtra("Assessment", assessment)
        startActivity(myIntent)
        finish()


        /*
            val map = mapOf("learningLevel" to "PARAGRAPH", "storyWordsWrong" to story_words_wrong)

          showProgress(true)
            assessmentDocumentSnapshot!!.reference.set(map, SetOptions.merge()).addOnSuccessListener {


                //updating student learning level
                val map2 = mapOf("learningLevel" to "PARAGRAPH")
                studentDocumentSnapshot!!.reference.set(map2, SetOptions.merge()).addOnSuccessListener {
                    showProgress(false)


                }


            }*/


     }

    fun getStory(key: String?): String {
        return when (key) {
            "3" -> {
                Assessment_Content.getS3()
            }
            "4" -> {
                Assessment_Content.getS4()
            }
            "5" -> {
                Assessment_Content.getS5()
            }
            "6" -> {
                Assessment_Content.getS6()
            }
            "7" -> {
                Assessment_Content.getS7()
            }
            "8" -> {
                Assessment_Content.getS8()
            }
            "9" -> {
                Assessment_Content.getS9()
            }
            "10" -> {
                Assessment_Content.getS10()
            }
            else -> Assessment_Content.getS3()
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