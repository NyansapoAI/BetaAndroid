package com.example.edward.nyansapo.presentation.ui.select_task

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentSelectTaskBinding
import com.example.edward.nyansapo.presentation.ui.add_student.AddStudentFragment

class SelectTaskFragment : Fragment(R.layout.fragment_select_task) {
    private lateinit var binding: FragmentSelectTaskBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelectTaskBinding.bind(view)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding.recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(SelectItemDecoration())

            adapter = SelectTaskAdapter(getTasks()) {
                onItemClicked(it)
            }
        }
    }

    private fun onItemClicked(it: Int) {
        when (it) {
            1 -> {
                goToAddStudent()
            }
            2 -> {
                goToTakeAttendanceScreen()

            }
            3 -> {
                goToAssessmentScreen()
            }
            4 -> {
                goToNumeracyLearningLevelScreen()
            }
            5 -> {
                goToActivitiesScreen()
            }
        }

    }

    private fun goToActivitiesScreen() {
        findNavController().navigate(R.id.action_selectTaskFragment_to_activitiesFragment3)
    }

    private fun goToTakeAttendanceScreen() {
        findNavController().navigate(R.id.action_selectTaskFragment_to_attendanceFragment)
    }

    private fun goToNumeracyLearningLevelScreen() {
        findNavController().navigate(R.id.action_selectTaskFragment_to_numeracyLearningLevelFragment)
    }

    private fun goToAddStudent() {
        findNavController().navigate(R.id.action_selectTaskFragment_to_addStudentFragment)
    }

    private fun goToAssessmentScreen() {
        findNavController().navigate(R.id.action_selectTaskFragment_to_assessmentFragment)
    }

    private fun getTasks(): List<String> {
        val list = mutableListOf<String>()
        list.add("Register Student")
        list.add("Take Attendance")
        list.add("Assess Student  ")
        list.add("View Grouping  ")
        list.add("View All Activities  ")
     //   list.add("Create an Activity  ")
      //  list.add("Create Session Plan  ")
       // list.add("View Session Plan  ")

        return list

    }
}