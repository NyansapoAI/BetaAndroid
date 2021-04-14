package com.example.edward.nyansapo.data.models.ui.activities

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentActivitiesBinding
import com.example.edward.nyansapo.Learning_Level
import com.example.edward.nyansapo.data.models.ui.activities.ActivitiesFragment2.Event.ActivityClicked
import com.example.edward.nyansapo.data.models.ui.grouping.SwipeGestureListener
import com.example.edward.nyansapo.data.models.ui.grouping.SwipeListener
import com.example.edward.nyansapo.data.models.ui.main.MainActivity2
import com.example.edward.nyansapo.wrappers.Resource
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_learning_level.*
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class ActivitiesFragment2 : Fragment(R.layout.fragment_activities), SwipeListener {
    companion object {
        private const val TAG = "ActivitiesFragment"
    }

    val viewModel: ActivitiesViewModel by viewModels()
    lateinit var binding: FragmentActivitiesBinding
    lateinit var adapter: ActivitiesAdapter2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        binding = FragmentActivitiesBinding.bind(view)
        subScribeToObservers()
        setGestureListener()
        setUpToolbar()
        setUpTabLayout()
        setUpRecyclerView()
        setOnClickListeners()

    }

    private fun subScribeToObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.activitiesFlow.collect {
                Log.d(TAG, "subScribeToObservers::activitiesFlow status:${it.status}")
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        //     adapter.submitList(it.data)
                    }
                    Resource.Status.ERROR -> {
                        Toasty.error(requireContext(), it.exception!!.message!!).show()
                    }
                    Resource.Status.LOADING -> {
                        // NO OP
                    }

                }
            }

            viewModel.activitiesQueryStatus.collect {
                Log.d(TAG, "subScribeToObservers::activitiesQueryStatus status:${it.status}")
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        adapter.submitList(it.data)
                    }
                    Resource.Status.ERROR -> {
                        Toasty.error(requireContext(), it.exception!!.message!!).show()
                        showTabs(true)
                    }
                    Resource.Status.LOADING -> {
                        // NO OP
                    }

                }
            }


            Log.d(TAG, "subScribeToObservers: end ")
        }
        lifecycleScope.launchWhenStarted {

            viewModel.channelClickEvents.collect { event ->
                Log.d(TAG, "subScribeToObservers: channel click event")
                when (event) {
                    is Event.ActivityClicked -> {
                        goToDetailScreen(event.data)

                    }
                }
            }
        }



        viewModel.setEvent(Event.SearchLearningLevel(Learning_Level.BEGINNER.name))
    }

    private fun goToDetailScreen(activity: Activity) {
        Log.d(TAG, "goToDetailScreen: activity:$activity")
        val bundle = bundleOf("activity" to activity)
        val fragment = ActivitiesDetailFragment()
        fragment.arguments = bundle
        MainActivity2.activityContext!!.supportFragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    private fun setGestureListener() {
        binding.recyclerview.setOnTouchListener(SwipeGestureListener(this))
    }

    private fun setUpRecyclerView() {
        adapter = ActivitiesAdapter2(this, {
            onSearchViewEmpty()
        }) {
            onActivityClicked(it)
        }



        binding.recyclerview.layoutManager = LinearLayoutManager(MainActivity2.activityContext!!)
        binding.recyclerview.adapter = adapter
    }

    private fun onSearchViewEmpty() {
        //    setUpRecyclerView()
    }


    private fun onActivityClicked(it: Activity) {
        Log.d(TAG, "onActivityClicked: Activity:${it.name}")
        viewModel.setEvent(ActivityClicked(it))


    }

    private fun setOnClickListeners() {
        binding.fob.setOnClickListener {

            Log.d(TAG, "setOnClickListeners: ")
            viewModel.setEvent(Event.NoEvent)
        }
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
        val searchItem = (binding.toolbar.root.menu.findItem(R.id.searchItem))
        val searchView = searchItem.actionView as SearchView

        MenuItemCompat.setOnActionExpandListener(searchItem, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                Log.d(TAG, "onMenuItemActionCollapse: ")
                showTabs(true)

                onSwipeLeft()
                return true // Return true to collapse action view
            }

            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                Log.d(TAG, "onMenuItemActionExpand: ")
                return true // Return true to expand action view
            }
        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "onQueryTextChange: ")
                if (newText != null && newText.isNotEmpty()) {
                    showTabs(false)
                    viewModel.setEvent(Event.SearchQuery(newText?.toLowerCase()))

                } else {
                    Log.d(TAG, "onQueryTextChange: searchview empty")

                }
                return true
            }
        })
    }

    private fun showTabs(visible: Boolean) {
        binding.tabs.isVisible = visible
    }

    private fun setUpTabLayout() {
        //   binding.tabs.addTab(binding.tabs.newTab().setText("UNKNOWN"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Beginner"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Letter"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Word"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Paragraph"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Story"))
        //  binding.tabs.addTab(binding.tabs.newTab().setText("Above"))


        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position
                Log.d(TAG, "onTabSelected: $position")
                thisTabPositionHasBeenSelected(position!!)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })


    }

    private fun thisTabPositionHasBeenSelected(position: Int) {
        Log.d(TAG, "thisTabPositionHasBeenSelected: position:$position")
        when (position) {

            0 -> {
                initRecyclerViewAdapter(Learning_Level.BEGINNER.name)
            }
            1 -> {
                initRecyclerViewAdapter(Learning_Level.LETTER.name)
            }
            2 -> {
                initRecyclerViewAdapter(Learning_Level.WORD.name)
            }
            3 -> {
                initRecyclerViewAdapter(Learning_Level.PARAGRAPH.name)
            }
            4 -> {
                initRecyclerViewAdapter(Learning_Level.STORY.name)
            }
            5 -> {
                initRecyclerViewAdapter(Learning_Level.ABOVE.name)
            }
        }


    }

    private fun initRecyclerViewAdapter(learninglevel: String) {
        Log.d(TAG, "initRecyclerViewAdapter: ")
        viewModel.setEvent(Event.SearchLearningLevel(learninglevel.toLowerCase()))
    }

    override fun onSwipeLeft() {
        Log.d(TAG, "onSwipeLeft: ")
        Log.d(TAG, "onSwipeLeft: selectedTabPosition:${tabs.selectedTabPosition} : :tab size:${tabs.tabCount}")

        val position = (tabs.selectedTabPosition + 1) % tabs.tabCount
        tabs.getTabAt(position)!!.select()
        Log.d(TAG, "onSwipeLeft: position:$position")
    }

    override fun onSwipeLeft_Original(position: Int) {

    }

    override fun onSwipeRight() {
        Log.d(TAG, "onSwipeRight: ")
        Log.d(TAG, "onSwipeRight: selectedTabPosition:${tabs.selectedTabPosition} : :tab size:${tabs.tabCount}")

        Log.d(TAG, "onSwipeRight: ")
        var position = (tabs.selectedTabPosition - 1) % tabs.tabCount
        if (position < 0) {
            position = tabs.tabCount - 1
        }
        tabs.getTabAt(position)!!.select()
        Log.d(TAG, "onSwipeRight: position:$position")


    }

    sealed class Event {
        data class ActivityClicked(val data: Activity) : Event()
        data class SearchQuery(val query: String) : Event()
        data class SearchLearningLevel(val query: String) : Event()
        object NoEvent : Event()
    }

}