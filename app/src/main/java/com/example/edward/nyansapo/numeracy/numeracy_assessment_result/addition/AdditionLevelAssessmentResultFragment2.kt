package com.example.edward.nyansapo.numeracy.numeracy_assessment_result.addition

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentAdditionLevelAssessmentResult2Binding
import com.edward.nyansapo.databinding.FragmentAdditionLevelAssessmentResultBinding
import com.edward.nyansapo.databinding.FragmentBeginnerLevelAssessmentResultBinding
import com.example.edward.nyansapo.numeracy.AssessmentNumeracy
import com.example.edward.nyansapo.numeracy.Numeracy_Learning_Levels
import com.example.edward.nyansapo.numeracy.Problem

class AdditionLevelAssessmentResultFragment2 : Fragment(R.layout.fragment_addition_level_assessment_result2) {


    private val TAG = "AdditionLevelAssess"


    private lateinit var binding: FragmentAdditionLevelAssessmentResult2Binding
    private lateinit var assessMent: AssessmentNumeracy
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdditionLevelAssessmentResult2Binding.bind(view)
        arguments?.let {
            assessMent = it.getParcelable("assessment")!!
            Log.d(TAG, "onViewCreated:assessment:$assessMent ")
        }
        initRecyclerAdapter()
        setDefaults()
    }

    private lateinit var additionAdapter: AdditionAdapter
    private fun initRecyclerAdapter() {
        Log.d(TAG, "initRecyclerAdapter: list:$")
        additionAdapter = AdditionAdapter()
        binding.recyclerview.apply {
            adapter = additionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }


    private fun setDefaults() {
        when (assessMent.learningLevelNumeracy) {
            Numeracy_Learning_Levels.BEGINNER.name -> {
                Log.d(TAG, "setDefaults: beginner")
                beginnerLevel()

            }
            Numeracy_Learning_Levels.ADDITION.name -> {
                Log.d(TAG, "setDefaults: addition")
                additionLevel()
            }
            Numeracy_Learning_Levels.SUBTRACTION.name -> {
                Log.d(TAG, "setDefaults: subtraction")
                subtractionLevel()
            }
        }


    }

    private fun subtractionLevel() {
        Log.d(TAG, "subtractionLevel: ")
        additionLevel()
        list.add(Operation(assessMent.correctSubtraction, assessMent.correctSubtractionList, assessMent.wrongSubtractionList, "-"))
        additionAdapter.submitList(list)
        setHeader("Subtraction Level")
        Log.d(TAG, "subtractionLevel: list size:${list.size}")

    }

    private lateinit var list: MutableList<Operation>
    private fun additionLevel() {
        Log.d(TAG, "additionLevel: ")

        beginnerLevel()
        list = mutableListOf<Operation>()
        list.add(Operation(assessMent.correctAddition, assessMent.correctAdditionList, assessMent.wrongAdditionList, "+"))
        additionAdapter.submitList(list)
        setHeader("Addition Level")

        Log.d(TAG, "additionLevel: list size:${list.size}")

    }


    private fun beginnerLevel() {
        Log.d(TAG, "beginnerLevel: ")
        setHeader("Beginner Level")

        binding.recyclerview.isVisible = false
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


    }

    private fun setHeader(header: String) {
        binding.tvBeginner.text = header
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