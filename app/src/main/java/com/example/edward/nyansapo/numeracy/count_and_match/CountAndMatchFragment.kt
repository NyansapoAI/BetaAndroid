package com.example.edward.nyansapo.numeracy.count_and_match

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentCountAndMatchBinding
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.numeracy.AssessmentNumeracy
import com.example.edward.nyansapo.numeracy.Numeracy_Learning_Levels
import com.example.edward.nyansapo.numeracy.count_and_match.CountAndMatchViewModel.Event
import com.example.edward.nyansapo.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CountAndMatchFragment : Fragment(R.layout.fragment_count_and_match) {

    private val TAG = "CountAndMatchFragment"

    private val TOTAL_NO_OF_BALLS = 12
    private lateinit var binding: FragmentCountAndMatchBinding

    @Inject
    lateinit var requestManager: RequestManager
    private val viewModel: CountAndMatchViewModel by viewModels()
    private lateinit var student: Student

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            student = it.getParcelable("student") as Student
            Log.d(TAG, "onViewCreated: student:$student")
        }
        binding = FragmentCountAndMatchBinding.bind(view)
        setDefaults()
        subScribeToObservers()
    }

    private fun setDefaults() {
        binding.imvAvatar.setImageResource(student.avatar)

    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getCountAndMatch.collect {
                    when (it.status) {
                        Resource.Status.SUCCESS -> {
                            setClickListenersForChoices()
                            setClickListenersForBalls()
                            initUI()
                        }
                        Resource.Status.ERROR -> {
                            showToastInfo(it.exception?.message!!)
                        }
                    }
                }
            }

            launch {
                viewModel.countAndMatchEvents.collect {
                    when (it) {
                        is Event.Next -> {
                            goToNext()
                        }

                        is Event.EnableChoices -> {
                            enableChoices()
                        }
                        is Event.Refresh -> {
                            refresh()
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


    private fun setClickListenersForBalls() {
        for (index in 1..TOTAL_NO_OF_BALLS) {
            val imageView = binding.root.findViewWithTag<ImageView>("ball_$index")
            imageView.setOnClickListener {
                requestManager.load(R.mipmap.ball_selected).into(imageView)
                viewModel.setEvent(Event.BallClicked)
            }

        }

    }

    private fun setClickListenersForChoices() {
        viewModel.getCountAndMatch.value.data!!.forEachIndexed { index, number ->
            val textView = binding.root.findViewWithTag<TextView>("choice_${index + 1}")
            textView.setOnClickListener {
                choiceClicked(number)
            }

        }


        binding.skipTxtView.setOnClickListener {
            val assessmentNumeracy = AssessmentNumeracy().copy(correctCountAndMatch = viewModel.correctCount, student = student)

            goToNumberRecognition(assessmentNumeracy)
        }
    }

    private fun finishedFailed(correctList: MutableList<Int>, wrongList: MutableList<Int>) {
        val assessmentNumeracy = AssessmentNumeracy().copy(correctCountAndMatch = viewModel.correctCount, correctCountAndMatchList = correctList, wrongCountAndMatchList = wrongList, student = student)

        if (assessmentNumeracy.learningLevelNumeracy.equals(Numeracy_Learning_Levels.UNKNOWN.name)) {
            student.learningLevelNumeracy = Numeracy_Learning_Levels.BEGINNER.name
            assessmentNumeracy.learningLevelNumeracy = Numeracy_Learning_Levels.BEGINNER.name
        }

         goToNumberRecognition(assessmentNumeracy.copy(student=student))
    }

    private fun finishedPassed(correctList: MutableList<Int>, wrongList: MutableList<Int>) {
        val assessmentNumeracy = AssessmentNumeracy().copy(correctCountAndMatch = viewModel.correctCount, correctCountAndMatchList = correctList, wrongCountAndMatchList = wrongList, student = student)
        goToNumberRecognition(assessmentNumeracy)
    }

    private fun goToNumberRecognition(assessmentNumeracy: AssessmentNumeracy) {
        findNavController().navigate(CountAndMatchFragmentDirections.actionCountAndMatchFragmentToNumberRecognition2Fragment(assessmentNumeracy))

    }

    private fun choiceClicked(number: Int) {
        viewModel.setEvent(Event.ChoiceClicked(number))

    }

    private fun goToNext() {
        viewModel.setEvent(Event.Refresh)
    }

    private fun refresh() {
        hideBalls()
        disableChoices()
        displayBalls()
    }

    private fun hideBalls() {
        for (index in 1..TOTAL_NO_OF_BALLS) {
            val imageView = binding.root.findViewWithTag<ImageView>("ball_$index")
            imageView.isVisible = false
            requestManager.load(R.mipmap.ball_unselected).into(imageView)
        }

    }

    private fun initUI() {
        setChoices()
        displayBalls()
    }

    private fun setChoices() {
        viewModel.getCountAndMatch.value.data!!.forEachIndexed { index, i ->
            val textView = binding.root.findViewWithTag<TextView>("choice_${index + 1}")
            textView.text = i.toString()

        }
    }

    private fun displayBalls() {
        Log.d(TAG, "displayBalls: getCurrentNumber:${viewModel.getCurrent()}")
        Log.d(TAG, "displayBalls: counter:${viewModel.counter}")
        Log.d(TAG, "displayBalls: correctCount:${viewModel.correctCount}")

        for (i in 1..viewModel.getCurrent()) {
            showBall(i)
        }
    }

    private fun showBall(i: Int) {
        val imageView = binding.root.findViewWithTag<ImageView>("ball_$i")

        imageView.isVisible = true
        requestManager.load(R.mipmap.ball_unselected).into(imageView)
    }

    private fun enableChoices() {
        viewModel.getCountAndMatch.value.data!!.forEachIndexed { index, i ->
            val textView = binding.root.findViewWithTag<TextView>("choice_${index + 1}")
            textView.isEnabled = true
        }
    }

    private fun disableChoices() {
        viewModel.getCountAndMatch.value.data!!.forEachIndexed { index, i ->
            val textView = binding.root.findViewWithTag<TextView>("choice_${index + 1}")
            textView.isEnabled = false
        }
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }


}