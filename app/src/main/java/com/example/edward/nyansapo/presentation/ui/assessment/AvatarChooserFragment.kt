package com.example.edward.nyansapo.presentation.ui.assessment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.edward.nyansapo.R
import com.example.edward.nyansapo.select_assessment.SelectAssessment
import com.edward.nyansapo.databinding.ActivityBeginAssessMentChooserBinding
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.presentation.ui.main.MainActivity2
import com.example.edward.nyansapo.util.GlobalData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_begin_assess_ment_chooser.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AvatarChooserFragment : Fragment(R.layout.activity_begin_assess_ment_chooser), View.OnClickListener {

    private val TAG = "AvatarChooserFragment"

    lateinit var binding: ActivityBeginAssessMentChooserBinding
    private val viewModel: AvatarChooserViewModel by viewModels()
    private val navArgs: AvatarChooserFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ActivityBeginAssessMentChooserBinding.bind(view)
        setOnClickListeners()
        subScribeToObservers()
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.avatarChooserEvents.collect {
                    when (it) {
                        is Event.GoSelectAssessment -> {
                            goToSelectAssessment(it.student)
                        }
                    }
                }
            }
        }
    }

    private fun setOnClickListeners() {
        binding.apply {
            cheetaImageView.setOnClickListener(this@AvatarChooserFragment)
            lionImageView.setOnClickListener(this@AvatarChooserFragment)
            rhinoImageView.setOnClickListener(this@AvatarChooserFragment)
            buffaloImageView.setOnClickListener(this@AvatarChooserFragment)
            elephantImageView.setOnClickListener(this@AvatarChooserFragment)
        }
    }

    override fun onClick(view: View?) {
        viewModel.setEvent(Event.AvatarSelected(view?.id))
    }

    private fun goToSelectAssessment(student: Student) {
        findNavController().navigate(AvatarChooserFragmentDirections.actionAvatarChooserFragment2ToSelectAssessmentFragment2(student))
    }

    sealed class Event {
        data class AvatarSelected(val viewId: Int?) : Event()
        data class GoSelectAssessment(val student: Student) : Event()
    }
}