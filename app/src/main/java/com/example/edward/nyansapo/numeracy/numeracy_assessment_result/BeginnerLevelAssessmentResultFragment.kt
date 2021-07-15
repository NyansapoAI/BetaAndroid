package com.example.edward.nyansapo.numeracy.numeracy_assessment_result

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentBeginnerLevelAssessmentResultBinding

class BeginnerLevelAssessmentResultFragment:Fragment(R.layout.fragment_beginner_level_assessment_result) {
    private lateinit var binding:FragmentBeginnerLevelAssessmentResultBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentBeginnerLevelAssessmentResultBinding.bind(view)
    }
}