package com.example.edward.nyansapo.presentation.ui.assessment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.arlib.floatingsearchview.FloatingSearchView.*
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.edward.nyansapo.R
import com.example.edward.nyansapo.Student
import com.edward.nyansapo.databinding.FragmentAssessmentBinding
import com.example.edward.nyansapo.presentation.ui.main.MainActivity2
import com.example.edward.nyansapo.AddStudentFragment
import com.example.edward.nyansapo.presentation.ui.grouping.GroupingAdapter2
import com.example.edward.nyansapo.presentation.ui.main.campId
import com.example.edward.nyansapo.presentation.ui.main.groupId
import com.example.edward.nyansapo.presentation.ui.main.programId
import com.example.edward.nyansapo.util.Resource
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AssessmentFragment2 : Fragment(R.layout.fragment_assessment) {

    private val TAG = "AssessmentFragment"
    lateinit var binding: FragmentAssessmentBinding
    private lateinit var recyclerAdapter: GroupingAdapter2
    private val viewModel: AssessmentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAssessmentBinding.bind(view)
        setOnClickListeners()
        setUpRecyclerView()
        subscribeToObservers()

        val programId = MainActivity2.sharedPref.programId
        val groupId = MainActivity2.sharedPref.groupId
        val campId = MainActivity2.sharedPref.campId

        viewModel.setEvent(Event.FetchStudents(programId!!, groupId!!, campId!!))

    }

    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.fetchAllStudentsStatus.collect {
                    Log.d(TAG, "subscribeToObservers: fetchAllStudentsStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.SUCCESS -> {
                            setupSearchBar()
                            viewModel.setEvent(Event.GetSuggestionsRecycler)
                        }
                        Resource.Status.EMPTY -> {
                            showToastInfo("Database is Empty")
                        }
                        Resource.Status.ERROR -> {
                            showToastInfo("Error:${it.exception?.message}")

                        }
                    }
                }
            }

            launch {
                viewModel.fetchStudentsQuerysStatus.collect {
                    Log.d(TAG, "subscribeToObservers:fetchStudentsQuerysStatus:${it.status.name} ")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            binding.mSearchView.showProgress()
                        }
                        Resource.Status.SUCCESS -> {
                            binding.mSearchView.swapSuggestions(it.data)
                            binding.mSearchView.hideProgress()
                        }
                        Resource.Status.EMPTY -> {
                            binding.mSearchView.hideProgress()
                            showToastInfo("Results not found")

                        }
                        Resource.Status.ERROR -> {
                            binding.mSearchView.hideProgress()
                            showToastInfo("Error:${it.exception?.message}")

                        }
                    }
                }
            }

            launch {
                viewModel.getSuggestionsRecyclerStatus.collect {
                    Log.d(TAG, "subscribeToObservers:getSuggestionsStatus:${it.status.name} ")
                    suggestionsRecyclerReceived(it.data!!)

                }
            }
            launch {
                viewModel.getSuggestionsSearchStatus.collect {
                    Log.d(TAG, "subscribeToObservers:getSuggestionsStatus:${it.status.name} ")
                    suggestionsSearchReceived(it.data!!)

                }
            }

            launch {
                viewModel.assessmentEvents.collect {
                    when (it) {
                        is Event.StudentClicked -> {
                            onStudentClicked(it.student)
                        }
                    }
                }
            }
        }
    }

    private fun suggestionsRecyclerReceived(data: List<DocumentSnapshot>) {
        setDataToRecyclerView(data)
    }

    private fun suggestionsSearchReceived(data: List<Student>) {
        binding.mSearchView.swapSuggestions(data)
        binding.mSearchView.hideProgress()
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    override fun onResume() {
        super.onResume()
        if (binding.mSearchView.isSearchBarFocused) {
            Log.d(TAG, "onResume: searchview is focused")
            recyclerViewVisibility(false)
        }
    }

    private fun setUpRecyclerView() {
        recyclerAdapter = GroupingAdapter2(requireContext(),onStudentClick = { onStudentClicked(it.toObject(Student::class.java)!!) })
        binding.recyclerview.apply {
            layoutManager = LinearLayoutManager(MainActivity2.activityContext)
            adapter = recyclerAdapter
        }
    }

    private fun setDataToRecyclerView(data: List<DocumentSnapshot>) {
        recyclerAdapter.submitList(data)
    }

    private fun setOnClickListeners() {
        binding.fob.setOnClickListener {
            addStudent()
        }
    }

    private fun addStudent() {
        val intent = Intent(MainActivity2.activityContext!!, AddStudentFragment::class.java)
        startActivity(intent)
    }


    private fun setupSearchBar() {
        binding.mSearchView.setOnQueryChangeListener { oldQuery, newQuery ->

            viewModel.setEvent(Event.FetchStudentsQuery(newQuery))

        }




        binding.mSearchView.setOnSearchListener(object : OnSearchListener {
            override fun onSuggestionClicked(searchSuggestion: SearchSuggestion) {
                val student = searchSuggestion as Student

                viewModel.setEvent(Event.StudentClicked(student))

                Log.d(TAG, "onSuggestionClicked()")

            }

            override fun onSearchAction(query: String) {
                Log.d(TAG, "onSearchAction: ")
                //when keyboard search key is pressed
            }
        })
        binding.mSearchView.setOnFocusChangeListener(object : OnFocusChangeListener {
            override fun onFocus() {
                recyclerViewVisibility(false)
                binding.mSearchView.showProgress()
                viewModel.setEvent(Event.GetSuggestionsSearch)
            }

            override fun onFocusCleared() {
                recyclerViewVisibility(true)


            }
        })


    }

    private fun recyclerViewVisibility(visible: Boolean) {
        binding.listLinLayout.isVisible = visible
    }

    private fun onStudentClicked(student: Student) {
        findNavController().navigate(AssessmentFragment2Directions.actionAssessmentFragment2ToBeginAssessmentFragment(student))
    }


    sealed class Event {
        data class FetchStudents(val programId: String, val groupId: String, val campId: String) : Event()
        object GetSuggestionsSearch : Event()
        object GetSuggestionsRecycler : Event()
        object AddStudent : Event()
        data class FetchStudentsQuery(val query: String) : Event()
        data class StudentClicked(val student: Student) : Event()
    }


}


