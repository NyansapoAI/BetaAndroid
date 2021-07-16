package com.example.edward.nyansapo.numeracy.numeracy_assessment_result.addition

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentAdditionLevelAssessmentResultBinding
import com.edward.nyansapo.databinding.FragmentBeginnerLevelAssessmentResultBinding
import com.example.edward.nyansapo.numeracy.AssessmentNumeracy
import com.example.edward.nyansapo.numeracy.Problem

class AdditionLevelAssessmentResultFragment : Fragment(R.layout.fragment_addition_level_assessment_result) {

    private val TAG = "AdditionLevelAssessment"

    private lateinit var binding: FragmentAdditionLevelAssessmentResultBinding
    private lateinit var assessMent: AssessmentNumeracy
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdditionLevelAssessmentResultBinding.bind(view)
        arguments?.let {
            assessMent = it.getParcelable("assessment")!!
            Log.d(TAG, "onViewCreated:assessment:$assessMent ")
        }
        setDefaults()
    }


    private fun setDefaults() {
        try {
            setNumberRecognitionResults()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            setCountAndMatchResults()

        } catch (e: Exception) {
            e.printStackTrace()
        }


        setAdditionResults()


    }


    private fun setNumberRecognitionResults() {
        val correctWrongList = mutableListOf<Int>()
        correctWrongList.addAll(assessMent.correctNumberRecognitionList)
        correctWrongList.addAll(assessMent.wrongNumberRecognitionList)
        for (i in 0..assessMent.correctNumberRecognition - 1) {
            val textView = binding.root.findViewWithTag<TextView>("tvNumberRecogn_$i")
            val number = correctWrongList.get(i)
            textView.text = "$number"
            textView.setTextColor(Color.BLACK)
            textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_number_recog)
        }
        for (i in assessMent.correctNumberRecognition..4) {
            val textView = binding.root.findViewWithTag<TextView>("tvNumberRecogn_$i")
            val number = correctWrongList.get(i)
            textView.text = "$number"
            textView.setTextColor(Color.RED)
            textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_number_recog_red)
        }
    }

    private fun setCountAndMatchResults() {
        val correctWrongList = mutableListOf<Int>()
        correctWrongList.addAll(assessMent.correctCountAndMatchList)
        correctWrongList.addAll(assessMent.wrongCountAndMatchList)
        for (i in 0..assessMent.correctCountAndMatch - 1) {
            val textView = binding.root.findViewWithTag<TextView>("tvCountAndMatch_$i")
            val number = correctWrongList.get(i)
            textView.text = "$number"
            textView.setTextColor(Color.BLACK)
            textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_number_recog)
        }
        for (i in assessMent.correctCountAndMatch..4) {
            val textView = binding.root.findViewWithTag<TextView>("tvCountAndMatch_$i")
            val number = correctWrongList.get(i)
            textView.text = "$number"
            textView.setTextColor(Color.RED)
            textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_number_recog_red)
        }
    }

    private fun setAdditionResults() {
        val correctWrongList = mutableListOf<Problem>()
        correctWrongList.addAll(assessMent.correctAdditionList)
        correctWrongList.addAll(assessMent.wrongAdditionList)
        for (i in 0..assessMent.correctAddition - 1) {
            val tvFirst = binding.root.findViewWithTag<TextView>("tvAdditionFirst_$i")
            val tvSecond = binding.root.findViewWithTag<TextView>("tvAdditionSecond_$i")
            val tvAnswer = binding.root.findViewWithTag<TextView>("tvAdditionAnswer_$i")
            val problem = correctWrongList.get(i)
            tvFirst.text = "${problem.first}"
            tvSecond.text = "${problem.second}"
            tvAnswer.text = "${problem.answer}"
            tvAnswer.setTextColor(Color.BLACK)
            tvAnswer.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_number_recog)
        }
        for (i in assessMent.correctAddition..2) {
            val tvFirst = binding.root.findViewWithTag<TextView>("tvAdditionFirst_$i")
            val tvSecond = binding.root.findViewWithTag<TextView>("tvAdditionSecond_$i")
            val tvAnswer = binding.root.findViewWithTag<TextView>("tvAdditionAnswer_$i")
            val problem = correctWrongList.get(i)
            tvFirst.text = "${problem.first}"
            tvSecond.text = "${problem.second}"
            tvAnswer.text = "${problem.answer}"
            tvAnswer.setTextColor(Color.RED)
            tvAnswer.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_number_recog_red)
        }
    }
}