package com.example.edward.nyansapo.presentation.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.homeEvents.collect {
                    when (it) {
                        is Event.Performance -> {
                            goToPerformance()
                        }
                        is Event.Tasks -> {
                            goToTasks()
                        }
                        is Event.Settings -> {
                            goToSettings()
                        }
                        is Event.Change -> {
                            goToChange()
                        }
                    }
                }
            }
        }
    }


    private fun setOnClickListeners() {
        binding.apply {
            ivPerformance.setOnClickListener {
                viewModel.setEvent(Event.Performance)
            }
            ivTasks.setOnClickListener {
                viewModel.setEvent(Event.Tasks)
            }
            ivSettings.setOnClickListener {
                viewModel.setEvent(Event.Settings)
            }
            ivChange.setOnClickListener {
                viewModel.setEvent(Event.Change)
            }
        }
    }

    sealed class Event {
        object Performance : Event()
        object Tasks : Event()
        object Settings : Event()
        object Change : Event()
    }
}