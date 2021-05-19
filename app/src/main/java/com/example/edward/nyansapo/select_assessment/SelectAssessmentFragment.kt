package com.example.edward.nyansapo.select_assessment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ActivitySelectAssesmentBinding
import com.edward.nyansapo.databinding.ActivitySelectAssessmentBinding
import com.example.edward.nyansapo.Student
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.modal_layout.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelectAssessmentFragment : Fragment(R.layout.activity_select_assessment), View.OnClickListener {
    private lateinit var binding: ActivitySelectAssessmentBinding
    private val viewModel: SelectAssessmentViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ActivitySelectAssessmentBinding.bind(view)
        setOnClickListeners()
        subScribeToObservers()
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.selectAssessmentEvents.collect {
                    when(it){
                        is Event.GoToPreAssessment->{
                            goToPreAssessment(it.assessmentKey,it.student)
                        }
                    }
                }
            }
        }
    }

    private fun goToPreAssessment(assessmentKey: Int, student: Student) {

    }

    private fun setOnClickListeners() {

        binding.apply {
            assessment3Button.setOnClickListener(this@SelectAssessmentFragment)
            assessment4Button.setOnClickListener(this@SelectAssessmentFragment)
            assessment5Button.setOnClickListener(this@SelectAssessmentFragment)
            assessment6Button.setOnClickListener(this@SelectAssessmentFragment)
            assessment7Button.setOnClickListener(this@SelectAssessmentFragment)
            assessment8Button.setOnClickListener(this@SelectAssessmentFragment)
            assessment9Button.setOnClickListener(this@SelectAssessmentFragment)
            assessment10Button.setOnClickListener(this@SelectAssessmentFragment)
        }
    }

    override fun onClick(v: View?) {
        viewModel.setEvent(Event.AssessmentClicked(v?.id))
    }

    sealed class Event {
        data class AssessmentClicked(val viewId: Int?) : Event()
        data class GoToPreAssessment(val assessmentKey: Int, val student: Student) : Event()

    }
}