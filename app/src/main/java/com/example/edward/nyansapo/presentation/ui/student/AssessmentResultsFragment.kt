package com.example.edward.nyansapo.presentation.ui.student


import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ActivityIndividualStudentPageBinding
import com.example.edward.nyansapo.Assessment
import com.example.edward.nyansapo.Assessment_Content
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.db.AssessmentDao
import com.example.edward.nyansapo.presentation.ui.main.MainActivity2
import com.example.edward.nyansapo.presentation.utils.assessmentDocumentSnapshot
import com.example.edward.nyansapo.presentation.utils.studentDocumentSnapshot
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_individual_student_page.view.*
import java.io.File
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject


@AndroidEntryPoint
class AssessmentResultsFragment : Fragment(R.layout.activity_individual_student_page) {


    private val TAG = "AssessmentResultsFragme"

    lateinit var mediaPlayer: MediaPlayer

    @Inject
    lateinit var assessmentDao: AssessmentDao
    lateinit var binding: ActivityIndividualStudentPageBinding
    lateinit var student: Student
    lateinit var assessment: Assessment

    private var visible = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = ActivityIndividualStudentPageBinding.bind(view)
        student = studentDocumentSnapshot!!.toObject(Student::class.java)!!
        assessment = assessmentDocumentSnapshot!!.toObject(Assessment::class.java)!!
        setOnClickListeners()
        setupToolBar()
        underLineHeaderTxt()
        setAssessmentInfoToUi()
        setUpNumberPicker()

    }

    private fun setUpNumberPicker() {


        val directoryParagraph = File(Environment.getExternalStorageDirectory().absolutePath + "/nyansapo_recording/paragraphs/${studentDocumentSnapshot!!.id}/${assessment.id}")
        Log.d(TAG, "setUpNumberPicker: directory:${directoryParagraph.absolutePath}")

        /*  if(directoryParagraph.listFiles()!=null){
              binding.numberPickerParagraph.minValue = 0
              binding.numberPickerParagraph.maxValue = directoryParagraph.listFiles().size-1
              Log.d(TAG, "setUpNumberPicker: list_files:${directoryParagraph.listFiles().size}")
              binding.numberPickerParagraph.setOnValueChangedListener { picker, oldVal, newVal ->
                  Log.d(TAG, "setUpNumberPicker: newValue:$newVal")

                  readParagraphNew(newVal.toString())
              }
          }
  */


        val directoryStory = File(Environment.getExternalStorageDirectory().absolutePath + "/nyansapo_recording/storys/${studentDocumentSnapshot!!.id}/${assessment.id}")
        Log.d(TAG, "setUpNumberPicker: directory:${directoryStory.absolutePath}")

        if (directoryStory.listFiles() != null) {
            binding.numberPickerStory.minValue = 1
            binding.numberPickerStory.maxValue = directoryStory.listFiles().size
            Log.d(TAG, "setUpNumberPicker: list_files:${directoryStory.listFiles().size - 1}")
            binding.numberPickerStory.setOnValueChangedListener { picker, oldVal, newVal ->
                Log.d(TAG, "setUpNumberPicker: newValue:$newVal")
                val position = newVal - 1
                readStory(position.toString())
            }
        }
    }



    val wordClicked = object : View.OnClickListener {
        override fun onClick(view: View?) {
            val v = view as TextView
            readWord(v)

        }
    }


    val letterClicked = object : View.OnClickListener {
        override fun onClick(view: View?) {
            val v = view as TextView
            readLetter(v)

        }
    }

    private fun setOnClickListeners() {
        binding.apply {
            letter0.setOnClickListener(letterClicked)
            letter1.setOnClickListener(letterClicked)
            letter2.setOnClickListener(letterClicked)
            letter3.setOnClickListener(letterClicked)
            letter4.setOnClickListener(letterClicked)
            letter5.setOnClickListener(letterClicked)

        }

        binding.apply {
            word0.setOnClickListener(wordClicked)
            word1.setOnClickListener(wordClicked)
            word2.setOnClickListener(wordClicked)
            word3.setOnClickListener(wordClicked)
            word4.setOnClickListener(wordClicked)
            word5.setOnClickListener(wordClicked)
        }

        binding.visibilityImageview.setOnClickListener {
            visible = !visible

            if (visible) {
                visibleMode()
            } else {
                invisibleMode()
            }
            setAssessmentInfoToUi()
        }


    }

    private fun visibleMode() {
        binding.visibilityImageview.setImageResource(R.drawable.ic_visible)
    }

    private fun invisibleMode() {
        binding.visibilityImageview.setImageResource(R.drawable.ic_invisible)
    }


    private fun readLetter(v: TextView) {
        Log.d(TAG, "readLetter: reading the paragraph")

        var mediaPlayer: MediaPlayer
        try {
            val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/nyansapo_recording/letters/${studentDocumentSnapshot!!.id}/${assessment?.id}")
            val file = File(directory, "${v.text.toString()}.wav")

            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(file.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                Log.d(TAG, "readLetter: completed playing")
                mediaPlayer.release()
            }
        } catch (e: IOException) {
            Toasty.info(requireContext(), "Recoding not Found").show()
            Log.d(TAG, "readLetter: error")
            e.printStackTrace()
        }

    }


    private fun readWord(v: TextView) {
        Log.d(TAG, "readWord: reading the paragraph")
        setUpMediaPlayer()
        try {
            val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/nyansapo_recording/words/${studentDocumentSnapshot!!.id}/${assessment?.id}")
            val file = File(directory, "${v.text.toString()}.wav")

            mediaPlayer.setDataSource(file.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                Log.d(TAG, "readWord: completed playing")
                mediaPlayer.release()
            }
        } catch (e: IOException) {
            Toasty.info(requireContext(), "Recoding not Found").show()
            Log.d(TAG, "readWord: error")
            e.printStackTrace()
        }
    }
    private fun readParagraphNew(sentence_count: String) {
        Log.d(TAG, "readParagraphNew: reading the story")

        setUpMediaPlayer()

        try {
            val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/nyansapo_recording/paragraphs/${studentDocumentSnapshot!!.id}/${assessment?.id}")
            val path = File(directory, "$sentence_count.wav").absolutePath

            mediaPlayer.setDataSource(path)
            mediaPlayer.prepare()
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                Log.d(TAG, "readParagraphNew: completed playing")
                mediaPlayer.release()
            }
        } catch (e: IOException) {
            Toasty.info(requireContext(), "Recording not Found").show()
            Log.d(TAG, "readParagraphNew: error")
            e.printStackTrace()
        }
    }
    private fun readStory(sentence_count: String) {
        Log.d(TAG, "readStory: reading the story")

        setUpMediaPlayer()

        try {
            val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/nyansapo_recording/storys/${studentDocumentSnapshot!!.id}/${assessment.id}")
            val path = File(directory, "$sentence_count.wav").absolutePath

            mediaPlayer.setDataSource(path)
            mediaPlayer.prepare()
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                Log.d(TAG, "readStory: completed playing")
                mediaPlayer.release()
            }
        } catch (e: IOException) {
            Toasty.info(requireContext(), "Recording not Found").show()
            Log.d(TAG, "readStory: error")
            e.printStackTrace()
        }
    }

    private fun setUpMediaPlayer() {
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.release()

        }
            mediaPlayer = MediaPlayer()



    }


    private fun underLineHeaderTxt() {
        binding.apply {
            letterHeader.paint.isUnderlineText = true
            wordHeader.paint.isUnderlineText = true
            paragraphHeader.paint.isUnderlineText = true
            storyHeader.paint.isUnderlineText = true
        }
    }

    private fun setupToolBar() {
        //setting up name of students

        val fullname = "${studentDocumentSnapshot!!.toObject(Student::class.java)!!.firstname}  ${studentDocumentSnapshot!!.toObject(Student::class.java)!!.lastname}"
        binding.toolbar.root.title = fullname

    }

    private fun setAssessmentInfoToUi() {
//first determine which level we are in
        when (assessment.learningLevel) {
            "UNKNOWN" -> {
                Toasty.info(MainActivity2.activityContext!!, "You assessment is unknown You might have started the assessment but didnt complete", Toasty.LENGTH_LONG).show()
            }
            "BEGINNER" -> setDataForBeginnerLevel()
            "LETTER" -> setDataForLetterLevel()
            "WORD" -> setDataForWordLevel()
            "PARAGRAPH" -> setDataForParagraphLevel()
            "STORY" -> setDataForStoryLevel()
            "ABOVE" -> setDataForAboveLevel()


        }
    }

    private fun setDataForAboveLevel() {
        Log.d(TAG, "setDataForAboveLevel:")
        binding.learningLevelImageView.setImageResource(R.mipmap.above_level)

        binding.letterLinearLayout.visibility = View.GONE
        binding.wordLinearLayout.visibility = View.GONE

        startSettingParagraphs()
        startSettingStory()
    }

    private fun setDataForBeginnerLevel() {
        Log.d(TAG, "setDataForBeginnerLevel: ")
        binding.learningLevelImageView.setImageResource(R.mipmap.beginner_level)

        binding.storyLinearLayout.visibility = View.GONE

        startSettingLetters()
        startSettingWords()
        startSettingParagraphs()

    }

    private fun setDataForParagraphLevel() {
        Log.d(TAG, "setDataForParagraphLevel: ")
        binding.learningLevelImageView.setImageResource(R.mipmap.paragraph_level)

        binding.letterLinearLayout.visibility = View.GONE
        binding.wordLinearLayout.visibility = View.GONE

        startSettingParagraphs()
        startSettingStory()


    }

    private fun startSettingQuestions(isParagraph: Boolean) {
        val questions = getQuestions(assessment.assessmentKey)

        binding.q1TxtView.text = "Q1. ${questions[0]} "
        binding.q2TxtView.text = "Q2. ${questions[1]} "


        if (isParagraph) {
            binding.answer1TxtView.setTextColor(Color.RED)
            binding.answer2TxtView.setTextColor(Color.RED)
        }

        binding.answer1TxtView.text = assessment.storyAnswerQ1
        binding.answer2TxtView.text = assessment.storyAnswerQ2
    }

    private fun startSettingStory() {
        Log.d(TAG, "startSettingStory: ")
        val wholeStory = getStory(assessment.assessmentKey)


        val wordtoSpan: Spannable = SpannableString(wholeStory)



        Log.d(TAG, "startSettingStory: ${assessment.storyWordsWrong}")

        assessment.storyWordsWrong.split(",", ignoreCase = true).forEach { string ->

            if (!string.isBlank()) {

                underLineThisWord(string.trim(), wholeStory, wordtoSpan)

            }
        }

        if (visible) {
            binding.storyTxtView.text = wordtoSpan
        } else {
            binding.storyTxtView.text = wholeStory


        }


        startSettingQuestions(false)

    }

    private fun setDataForStoryLevel() {
        Log.d(TAG, "setDataForStoryLevel: ")
        binding.learningLevelImageView.setImageResource(R.mipmap.story_level)

        binding.letterLinearLayout.visibility = View.GONE
        binding.wordLinearLayout.visibility = View.GONE

        startSettingParagraphs()
        startSettingStory()


    }

    private fun setDataForWordLevel() {
        Log.d(TAG, "setDataForWordLevel: ")
        binding.learningLevelImageView.setImageResource(R.mipmap.word_level)

        binding.letterLinearLayout.visibility = View.GONE
        binding.storyLinearLayout.visibility = View.GONE

        startSettingWords()
        startSettingParagraphs()

    }


    private fun setDataForLetterLevel() {
        Log.d(TAG, "setDataForLetterLevel: ")
        binding.learningLevelImageView.setImageResource(R.mipmap.letter_level)

        binding.storyLinearLayout.visibility = View.GONE

        startSettingLetters()
        startSettingWords()
        startSettingParagraphs()

    }

    private fun startSettingParagraphs() {
        Log.d(TAG, "startSettingParagraphs: ")
        val wholeParagraph = getPara(assessment.assessmentKey)[assessment.paragraphChoosen]

        val wordtoSpan: Spannable = SpannableString(wholeParagraph)


        Log.d(TAG, "startSettingParagraphs: paragraph words wrong ${assessment.paragraphWordsWrong}")

        assessment.paragraphWordsWrong.split(",", ignoreCase = true).forEach { string ->

            if (!string.isBlank()) {

                underLineThisWord(string.trim().toLowerCase(), wholeParagraph.toLowerCase(), wordtoSpan)

            }
        }

        //    binding.paragraphTxtView.text = wordtoSpan
        setParagraphOnButtons(wordtoSpan)

    }

    private fun setParagraphOnButtons(wordtoSpan: Spannable) {
        Log.d(TAG, "setParagraphOnButtons: ")
        wordtoSpan.split(".").filter {
            !it.isBlank()
        }.forEachIndexed { index, sentence ->
            Log.d(TAG, "setParagraphOnButtons: index:$index : :sentence:$sentence")

            underlineWordAndSetOnButton(index, sentence)


        }
    }

    val paragraphClickListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            Log.d(TAG, "onClick:${v?.id} button 0: :${R.id.button_0}")

            when (v?.id) {
                R.id.button_0 -> {
                    Log.d(TAG, "onClick: button 0")
                    readParagraphNew(0.toString())
                }
                R.id.button_1 -> {
                    readParagraphNew(1.toString())
                }
                R.id.button_2 -> {
                    readParagraphNew(2.toString())
                }
                R.id.button_3 -> {
                    readParagraphNew(3.toString())
                }
                R.id.button_4 -> {
                    readParagraphNew(4.toString())
                }
            }
        }
    }
private var paragraphWordsWrongList= mutableListOf<String>()
    private fun underlineWordAndSetOnButton(index: Int, sentence: String) {
        Log.d(TAG, "underlineWordAndSetOnButton: ")
        val wordsToSpan = SpannableString(sentence)

     paragraphWordsWrongList = assessment.paragraphWordsWrong.split(",", ignoreCase = true).toMutableList()

        paragraphWordsWrongList.forEach { string ->

            if (!string.isBlank()) {

                underLineThisWord(string.trim().toLowerCase(), sentence.toLowerCase(), wordsToSpan)

            }
        }


        val button: Button = binding.root.findViewWithTag("button_$index")

        if (visible) {
            button.text = wordsToSpan

        } else {
            button.text = sentence

        }
        button.visibility = View.VISIBLE

        button.setOnClickListener(paragraphClickListener)
    }

    private fun underLineThisWord(wordToSearch: String, wholeParagraph: String, spannable: Spannable) {
        Log.d(TAG, "underLineThisWord: started underlining words in paragraph/story")

        Log.d(TAG, "underLineThisWord: string::$wordToSearch wholeParagraph: :$wholeParagraph")
        val pattern = "\\b$wordToSearch\\b"
        val patternObject = Pattern.compile(pattern)
        val matcher = patternObject.matcher(wholeParagraph)

        if (matcher.find()) {
            high_light_found_text(spannable, matcher)
            //remove found word from list
          //  paragraphWordsWrongList.remove(wordToSearch)
        }

    }

    private fun high_light_found_text(wordsToSpan: Spannable, matcher: Matcher) {

        wordsToSpan.setSpan(ForegroundColorSpan(Color.RED), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    }

    private fun startSettingWords() {
        Log.d(TAG, "startSettingWords words wrong: ${assessment.wordsWrong}")
        Log.d(TAG, "startSettingWords words correct: ${assessment.wordsCorrect}")


        var lastIndex: Int = 0
        var flag: Boolean = true

        assessment.wordsWrong.split(",", ignoreCase = true).filter {
            !it.isBlank()
        }.forEachIndexed { index, string ->
            Log.d(TAG, "startSettingWords: index: $index word: $string")

            flag = false
            val textView: TextView = binding.root.findViewWithTag<TextView>("word_$index")
            if (visible) {
                textView.setBackgroundResource(R.drawable.bg_wrong_word)

            } else {
                textView.setBackgroundResource(R.drawable.bg_correct_word)

            }
            textView.text = string.trim()

            lastIndex = index

            Log.d(TAG, "startSettingWords: last index $lastIndex")
        }

        if (flag) {
            lastIndex = -1
        }
        assessment.wordsCorrect.split(",", ignoreCase = true).filter {
            !it.isBlank()
        }.forEachIndexed { index, string ->
            Log.d(TAG, "startSettingWords: index: $index word: $string")

            val pos = lastIndex + 1 + index
            if (pos == 6) {
                Log.d(TAG, "startSettingWords: returning")
                return@forEachIndexed
            }
            val textView: TextView = binding.root.findViewWithTag<TextView>("word_${pos}")

            textView.setBackgroundResource(R.drawable.bg_correct_word)

            textView.text = string.trim()

        }

    }

    private fun startSettingLetters() {
        Log.d(TAG, "startSettingLetters: ")



        Log.d(TAG, "startSettingLetters letter wrong: ${assessment.lettersWrong}")
        Log.d(TAG, "startSettingLetters letter correct: ${assessment.letterCorrect}")


        var lastIndex: Int = 0
        var flag: Boolean = true

        assessment.lettersWrong.split(",", ignoreCase = true).filter {
            !it.isBlank()
        }.forEachIndexed { index, string ->

            flag = false

            val textView: TextView = binding.root.findViewWithTag<TextView>("letter_$index")
            if (visible) {
                textView.setBackgroundResource(R.drawable.bg_wrong_word)
            } else {
                textView.setBackgroundResource(R.drawable.bg_correct_word)

            }
            textView.text = string.trim()
            Log.d(TAG, "startSettingLetters: index:$index letter:${textView.text}")
            lastIndex = index
        }

        Log.d(TAG, "startSettingLetters: lastIndex :$lastIndex")
        if (flag) {
            lastIndex = -1
        }

        assessment.letterCorrect.split(",", ignoreCase = true).filter {
            !it.isBlank()
        }.forEachIndexed { index, string ->

            val pos = lastIndex + 1 + index
            if (pos == 6) {
                return@forEachIndexed
            }
            val textView: TextView = binding.root.findViewWithTag<TextView>("letter_${pos}")

            textView.setBackgroundResource(R.drawable.bg_correct_word)

            textView.text = string.trim()
            Log.d(TAG, "startSettingLetters: pos:$pos letter:${textView.text}")

        }

    }

    private fun setParagraphSpannableString() {


        val wordtoSpan: Spannable = SpannableString("One morning, the cow went to the lion. She\n" +
                "wanted him to help her. The lion roared at them.\n" +
                "The cow and her calf ran away. They found a\n" +
                "man outside his house. The man loved the\n" +
                "animals. He made a cow shed for them. The cow\n" +
                "never went back to the forest.")

        wordtoSpan.setSpan(ForegroundColorSpan(Color.RED), 17, 29, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        wordtoSpan.setSpan(ForegroundColorSpan(Color.RED), 50, 61, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        wordtoSpan.setSpan(ForegroundColorSpan(Color.RED), 90, 100, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        wordtoSpan.setSpan(ForegroundColorSpan(Color.RED), 152, 167, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // binding.paragraphTxtView.text = wordtoSpan
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

    fun getQuestions(key: String?): Array<String> {
        return when (key) {
            "3" -> {
                Assessment_Content.getQ3()
            }
            "4" -> {
                Assessment_Content.getQ4()
            }
            "5" -> {
                Assessment_Content.getQ5()
            }
            "6" -> {
                Assessment_Content.getQ6()
            }
            "7" -> {
                Assessment_Content.getQ7()
            }
            "8" -> {
                Assessment_Content.getQ8()
            }
            "9" -> {
                Assessment_Content.getQ9()
            }
            "10" -> {
                Assessment_Content.getQ10()
            }
            else -> Assessment_Content.getQ3()
        }
    }



}