package com.example.edward.nyansapo.presentation.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.MenuItemCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentActivities3Binding
import com.edward.nyansapo.databinding.FragmentActivitiesBinding
import com.example.edward.nyansapo.Learning_Level
import com.example.edward.nyansapo.numeracy.numeracy_learning_level.Data
import com.example.edward.nyansapo.numeracy.numeracy_learning_level.LevelSectionsAdapter2
import com.example.edward.nyansapo.presentation.ui.activities.ActivitiesFragment2.Event.ActivityClicked
import com.example.edward.nyansapo.presentation.ui.grouping.SwipeGestureListener
import com.example.edward.nyansapo.presentation.ui.grouping.SwipeListener
import com.example.edward.nyansapo.presentation.ui.main.MainActivity2
import com.example.edward.nyansapo.util.Resource
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_learning_level.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ActivitiesFragment3 : Fragment(R.layout.fragment_activities3) {
    companion object {
        private const val TAG = "ActivitiesFragment"
    }

    val viewModel: ActivitiesViewModel by viewModels()
    lateinit var binding: FragmentActivities3Binding
    lateinit var activitiesAdapter: ActivitiesAdapter3

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        binding = FragmentActivities3Binding.bind(view)
        initRecyclerViewAdapter()
        subScribeToObservers()


    }
    private fun setUpToolbar() {
        binding.toolbar.root.inflateMenu(R.menu.search_menu)
        binding.toolbar.root.setTitle("Activities")
        binding.toolbar.root.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                R.id.addStudentItem -> {
                    //
                }
            }


            true
        }

        val searchView = (binding.toolbar.root.menu.findItem(R.id.searchItem).actionView as SearchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "onQueryTextChange: ")
                if (newText != null && newText.isNotEmpty()) {
                   // showTabs(false)
                    // adapter.filter.filter(newText?.toLowerCase())

                } else {
                    Log.d(TAG, "onQueryTextChange: searchview empty")
                   // setUpRecyclerView()
                  //  showTabs(true)
                }
                return true
            }
        })
    }
    private fun initRecyclerViewAdapter() {
         activitiesAdapter = ActivitiesAdapter3{ onActivityClicked(it) }
        binding.recyclerview.apply {
            setHasFixedSize(false)
            //  addItemDecoration(NumeracyItemDecoration())
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activitiesAdapter

        }
      //  levelSectionsAdapter.submitList(Data.getList())

    }

    private fun onActivityClicked(it: Activity) {
        Log.d(TAG, "onActivityClicked: ")
        findNavController().navigate(ActivitiesFragment3Directions.actionActivitiesFragment3ToActivitiesDetailFragment(it))
    }


    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getActivitiesStatus.collect {
                    when (it.status) {
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.SUCCESS -> {

                        }
                        Resource.Status.ERROR -> {

                        }
                    }
                }
            }

            launch {
                viewModel.wholeClassFlow.collect {
                    setWholeClassData(it)
                }
            }
            launch {
                viewModel.beginnerFlow.collect {
                    setBeginnerData(it)
                }
            }
            launch {
                viewModel.letterFlow.collect {
                    setLetterFlow(it)
                }
            }
            launch {
                viewModel.wordFlow.collect {
                    setWordFlow(it)
                }
            }
            launch {
                viewModel.paragraphFlow.collect {
                    setParagraphData(it)
                }
            }
            launch {
                viewModel.storyFlow.collect {
                    setStoryData(it)
                }
            }
        }
    }

    private fun setWholeClassData(it: List<Activity>) {
        Log.d(TAG, "setWholeClassData: size:${it.size}")
        val newList = Data.getList2()
        newList[0].sectionActivities = it.toMutableList()
        activitiesAdapter.submitList(newList)
    }

    private fun setBeginnerData(it: List<Activity>) {
        Log.d(TAG, "setBeginnerData: size:${it.size}")
        val newList = Data.getList2()
        newList[1].sectionActivities = it.toMutableList()
        activitiesAdapter.submitList(newList)
    }

    private fun setLetterFlow(it: List<Activity>) {
        Log.d(TAG, "setLetterFlow: size:${it.size}")
        val newList = Data.getList2()
        newList[2].sectionActivities = it.toMutableList()
        activitiesAdapter.submitList(newList)
    }

    private fun setWordFlow(it: List<Activity>) {
        Log.d(TAG, "setWordFlow: size:${it.size}")
        val newList = Data.getList2()
        newList[3].sectionActivities = it.toMutableList()
        activitiesAdapter.submitList(newList)
    }

    private fun setParagraphData(it: List<Activity>) {
        Log.d(TAG, "setParagraphData: size:${it.size}")
        val newList = Data.getList2()
        newList[4].sectionActivities = it.toMutableList()
        activitiesAdapter.submitList(newList)
    }

    private fun setStoryData(it: List<Activity>) {
        Log.d(TAG, "setStoryData: size:${it.size}")
        val newList = Data.getList2()
        newList[5].sectionActivities = it.toMutableList()
        activitiesAdapter.submitList(newList)
    }
}
