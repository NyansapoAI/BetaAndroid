package com.example.edward.nyansapo.presentation.ui.select_task

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentSelectTaskBinding

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

            }
            2 -> {

            }
            3 -> {

            }
            4 -> {

            }
        }

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