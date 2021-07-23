package com.example.edward.nyansapo.numeracy.numeracy_assessment_result

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentNumeracyAssessmentResultBinding
import com.example.edward.nyansapo.numeracy.numeracy_assessment_result.addition.AdditionLevelAssessmentResultFragment
import com.example.edward.nyansapo.numeracy.numeracy_assessment_result.addition.AdditionLevelAssessmentResultFragment2
import com.example.edward.nyansapo.numeracy.numeracy_assessment_result.addition.SubtractionLevelAssessmentResultFragment
import com.example.edward.nyansapo.numeracy.numeracy_assessment_result.beginner.BeginnerLevelAssessmentResultFragment
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NumeracyAssessmentResultFragment : Fragment(R.layout.fragment_numeracy_assessment_result) {

    private val TAG = "NumeracyAssessmentResul"

    private val navArgs: NumeracyAssessmentResultFragmentArgs by navArgs()
    private val viewModel: NumeracyAssessmentResultViewModel by viewModels()
    private lateinit var binding: FragmentNumeracyAssessmentResultBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: choosen:${navArgs.choosen} ::student:${navArgs.student}")
        binding = FragmentNumeracyAssessmentResultBinding.bind(view)
        viewModel.setEvent(NumeracyAssessmentResultViewModel.Event.FetchAssessments(navArgs.student))
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.fetchAssessmentsStatus.collect {
                    Log.d(TAG, "subscribeToObservers:fetchAssessmentsStatus:${it.status.name} ")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)

                            viewModel.saveAssessMents(it.data!!)
                            setOnClickListeners()
                            setDefaults()
                            viewModel.setEvent(NumeracyAssessmentResultViewModel.Event.FirstScreen(navArgs.choosen))

                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)


                        }
                        Resource.Status.EMPTY -> {
                            showProgress(false)


                        }
                    }
                }
            }

            launch {
                viewModel.numeracyAssessmentResultEvents.collect {
                    Log.d(TAG, "subscribeToObservers:numeracyAssessmentResultEvents ")
                    val bundle = Bundle()
                    val event=it as NumeracyAssessmentResultViewModel.Event.DisplayAssessment
                    bundle.putParcelable("assessment", event.assessment)
                    Log.d(TAG, "subscribeToObservers: assessment:${event.assessment}")
                    requireActivity().supportFragmentManager.beginTransaction().replace(R.id.container, AdditionLevelAssessmentResultFragment2::class.java,bundle).commit()
                    setDefaults()
                }
            }


        }
    }

    private fun showProgress(visible: Boolean) {
        binding.progressBar.isVisible = visible
    }

    private fun setDefaults() {
        Log.d(TAG, "setDefaults: size:${viewModel.assessMentsListFlow.value.size}")
        val counter = viewModel.counter + 1
        val size = viewModel.assessMentsListFlow.value.size
        binding.tvAssessment.text = "Assessment $counter of $size"
    }

    private fun setOnClickListeners() {
        binding.apply {
            ivBack.setOnClickListener {
                backClicked()
            }

            ivFront.setOnClickListener {
                frontClicked()
            }
        }

    }

    private fun backClicked() {
        viewModel.setEvent(NumeracyAssessmentResultViewModel.Event.BackClicked)
    }

    private fun frontClicked() {
        viewModel.setEvent(NumeracyAssessmentResultViewModel.Event.FrontClicked)
    }


}