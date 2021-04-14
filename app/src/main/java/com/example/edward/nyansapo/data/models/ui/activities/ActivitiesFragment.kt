package com.example.edward.nyansapo.data.models.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentActivitiesBinding
import com.example.edward.nyansapo.Learning_Level
import com.example.edward.nyansapo.data.models.ui.grouping.SwipeGestureListener
import com.example.edward.nyansapo.data.models.ui.grouping.SwipeListener
import com.example.edward.nyansapo.data.models.ui.main.MainActivity2
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_learning_level.*


class ActivitiesFragment : Fragment(R.layout.fragment_activities), SwipeListener {
    companion object {
        private const val TAG = "ActivitiesFragment"
    }

    lateinit var binding: FragmentActivitiesBinding
    lateinit var adapter: ActivitiesAdapter
    var originalList: MutableList<Activity>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        binding = FragmentActivitiesBinding.bind(view)
      //  originalList = getList()
        setGestureListener()

        setUpToolbar()
        setUpTabLayout()
        setUpRecyclerView()
        setOnClickListeners()

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    private fun setGestureListener() {
        binding.recyclerview.setOnTouchListener(SwipeGestureListener(this))
    }

    private fun setUpRecyclerView() {
        adapter = ActivitiesAdapter(this, {
            onSearchViewEmpty()
        }) {
            onActivityClicked(it)
        }


        adapter.filter.filter(Learning_Level.BEGINNER.name)

        binding.recyclerview.layoutManager = LinearLayoutManager(MainActivity2.activityContext!!)
        binding.recyclerview.adapter = adapter
    }

    private fun onSearchViewEmpty() {
        //    setUpRecyclerView()
    }



    private fun onActivityClicked(it: Activity) {
        Log.d(TAG, "onActivityClicked: Activity:${it.name}")
        val bundle = bundleOf("activity" to it)
        val fragment = ActivitiesDetailFragment()
        fragment.arguments = bundle
        MainActivity2.activityContext!!.supportFragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit()

    }

    private fun setOnClickListeners() {
        binding.fob.setOnClickListener {
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

        val searchView = (binding.toolbar.root.menu.findItem(R.id.searchItem).actionView as SearchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "onQueryTextChange: ")
                if (newText != null && newText.isNotEmpty()) {
                    showTabs(false)
                    adapter.filter.filter(newText?.toLowerCase())

                } else {
                    Log.d(TAG, "onQueryTextChange: searchview empty")
                     setUpRecyclerView()
                    showTabs(true)
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

        adapter.filter.filter(learninglevel.toLowerCase())
    }

    override fun onSwipeLeft() {
        Log.d(TAG, "onSwipeLeft: ")
        Log.d(TAG, "onSwipeLeft: selectedTabPosition:${tabs.selectedTabPosition} : :tab size:${tabs.tabCount}")

        val position = (tabs.selectedTabPosition + 1) % tabs.tabCount
        tabs.getTabAt(position)!!.select()
        Log.d(TAG, "onSwipeLeft: position:$position")
    }

    override fun onSwipeLeft_Original(position: Int) {
        TODO("Not yet implemented")
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

}