package com.example.edward.nyansapo.presentation.ui.activities.create_activity

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentCreateActivityBinding
import com.example.edward.nyansapo.presentation.ui.activities.ActivitiesFragment
import com.example.edward.nyansapo.presentation.ui.activities.Activity
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.showProgress
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateActivityFragment : Fragment(R.layout.fragment_create_activity) {

    private val TAG = "CreateActivityFragment"

    private lateinit var binding: FragmentCreateActivityBinding
    private val viewModel: CreateActivityViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCreateActivityBinding.bind(view)
        setOnClickListeners()

        subScribeToObservers()
        setUpToolbar()
    }

    private fun setUpToolbar() {
        binding.toolbar.root.inflateMenu(R.menu.overflow_menu)
        binding.toolbar.root.setTitle("Create Activity")
        binding.toolbar.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.saveActivityStatus.collect {
                    Log.d(TAG, "subScribeToObservers: saveActivityStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            requireContext().showProgress(true)

                        }
                        Resource.Status.SUCCESS -> {
                            requireContext().showProgress(false)

                        }
                        Resource.Status.ERROR -> {
                            requireContext().showProgress(false)
                            showToastInfo(it.exception!!.message!!)
                        }
                    }
                }
            }
        }

    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun setOnClickListeners() {
        binding.btnCreate.setOnClickListener {
            createBtnClicked()
        }
    }

    private fun createBtnClicked() {
        val activity = getActivityObject()
        Log.d(TAG, "createBtnClicked: activity:$activity")
        viewModel.setEvent(CreateActivityViewModel.Event.CreateActivity(activity))
    }

    private fun getActivityObject(): Activity {
        val activity: Activity
        binding.apply {
            val activityName = edtActivityName.text.toString().trim()
            val activityDescription = edtDescription.text.toString().trim()
            val level = spLearningLevel.selectedItem.toString()

            activity = Activity().copy(name = activityName, steps = activityDescription, level = level)
        }

        return activity
    }


}