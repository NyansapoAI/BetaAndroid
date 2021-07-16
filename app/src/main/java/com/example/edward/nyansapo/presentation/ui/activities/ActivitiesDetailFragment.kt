package com.example.edward.nyansapo.presentation.ui.activities

import android.os.Bundle


import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentIndividualActivitiesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivitiesDetailFragment: Fragment(R.layout.fragment_individual_activities) {


    private val TAG = "IndividualActivitiesFra"

   private lateinit var binding: FragmentIndividualActivitiesBinding
    private lateinit var activity: Activity
    private val navArgs:ActivitiesDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentIndividualActivitiesBinding.bind(view)

        activity = navArgs.activity
        Log.d(TAG, "onViewCreated: activity:$activity")

        setDefaultValues()

    }

    private fun setDefaultValues() {
        binding.learningObjectivesTxtView.text = activity.learningObjectives
        binding.stepsTxtView.text = activity.steps
        binding.materialsTxtView.text = activity.materials
    }
}