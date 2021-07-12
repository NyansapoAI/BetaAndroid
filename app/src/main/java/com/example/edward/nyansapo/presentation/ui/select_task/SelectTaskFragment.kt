package com.example.edward.nyansapo.presentation.ui.select_task

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentSelectTaskBinding
import com.example.edward.nyansapo.AddStudentFragment

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

            }
            3 -> {
                goToAssessmentScreen()
            }
            4 -> {
                goToNumeracyLearningLevelScreen()
            }
        }

    }

    private fun goToNumeracyLearningLevelScreen() {
        findNavController().navigate(R.id.action_selectTaskFragment_to_numeracyLearningLevelFragment)
    }

    private fun goToAddStudent() {
        val intent = Intent(requireContext(), AddStudentFragment::class.java)
        startActivity(intent)
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
        list.add("Create an Activity  ")
        list.add("Create Session Plan  ")
        list.add("View Session Plan  ")

        return list

    }
}