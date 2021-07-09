package com.example.edward.nyansapo.presentation.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        setOnClickListeners()
    }


    private fun setOnClickListeners() {
        binding.apply {
            ivPerformance.setOnClickListener {
                goToPerformance()
            }
            ivTasks.setOnClickListener {
                goToTasks()
            }
            ivSettings.setOnClickListener {
                goToSettings()
            }
            ivChange.setOnClickListener {
                goToChange()
            }
        }
    }

    private fun goToChange() {
        findNavController().navigate(R.id.action_homeFragment_to_changeProgramFragment)
    }

    private fun goToSettings() {
        findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)

    }

    private fun goToTasks() {
        findNavController().navigate(R.id.action_homeFragment_to_selectTaskFragment)
    }

    private fun goToPerformance() {
        findNavController().navigate(R.id.action_homeFragment_to_patternsFragment)
    }


}