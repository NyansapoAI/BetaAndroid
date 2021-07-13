package com.example.edward.nyansapo.presentation.ui.grouping

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentLearningLevelBinding
import com.example.edward.nyansapo.presentation.ui.add_student.AddStudentFragment
import com.example.edward.nyansapo.Learning_Level
import com.example.edward.nyansapo.presentation.ui.main.MainActivity2
import com.example.edward.nyansapo.presentation.ui.student.StudentAssessmentListFragment
import com.example.edward.nyansapo.util.studentDocumentSnapshot
import com.example.edward.nyansapo.util.onQueryTextChanged
import com.example.edward.nyansapo.util.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_learning_level.*
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class GroupingFragment3 : Fragment(R.layout.fragment_learning_level), SwipeListener {


    private val TAG = "LearningLevelFragment"
    lateinit var adapter: GroupingAdapter3
    lateinit var listenerRegistration: ListenerRegistration
    lateinit var originalList: MutableList<DocumentSnapshot>


    lateinit var binding: FragmentLearningLevelBinding

    private val viewModel: GroupingViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLearningLevelBinding.bind(view)
        setUpToolbar()
        setUpTabLayout()
        setOnClickListeners()
        setGestureListener()
        setUpRecyclerView()
        subScribeToObservers()


    }

    private fun setUpRecyclerView() {
        Log.d(TAG, "setUpRecyclerView: ")
        adapter = GroupingAdapter3(this, {
            searchViewIsEmpty()
        },
                {
                    onStudentClicked(it)
                }, {
            onStudentLongClicked(it)
        })

        binding.recyclerview.setLayoutManager(LinearLayoutManager(MainActivity2.activityContext!!))
        binding.recyclerview.setAdapter(adapter)

    }


    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.fetchStudents.collect {
                Log.d(TAG, "subScribeToObservers:fetch students status:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        //loading data
                        showProgressBar(true)

                    }
                    Resource.Status.EMPTY -> {
                        //database is empty
                        showProgressBar(false)
                        databaseIsEmpty()
                    }
                    Resource.Status.SUCCESS -> {
                        //success loading data
                        showProgressBar(false)
                        //load data for the first tab
                        viewModel.setEvent(Event.StudentLearningLevelQuery(Learning_Level.UNKNOWN.name))

                    }
                    Resource.Status.ERROR -> {
                        //error trying to load data
                        showProgressBar(false)

                    }
                }
            }

        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.studentQueryStatus.collect {
                Log.d(TAG, "subScribeToObservers:student query  status:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        //loading data
                        showProgressBar(true)

                    }
                    Resource.Status.EMPTY -> {
                        //database is empty
                        showProgressBar(false)
                        databaseIsEmpty()
                    }
                    Resource.Status.SUCCESS -> {
                        //success loading data
                        showProgressBar(false)
                        setData(it.data)

                    }
                    Resource.Status.ERROR -> {
                        //error trying to load data
                        showProgressBar(false)

                    }
                }
            }

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.searchWidgetStatus.collect {
                when (it) {
                    is Event.SearchActive -> {
                        showTabs(false)
                    }
                    is Event.SearchNotActive -> {
                        showTabs(true)

                        viewModel.setEvent(Event.SwipeLeft(tabs.selectedTabPosition, tabs.tabCount))
                        //    onSwipeLeft()
                    }
                }
            }

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.swipeLeftAction.collect {
                swipeScreen(it.position)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.clickedStudentSnapshot.collect {
                MainActivity2.activityContext!!.supportFragmentManager.beginTransaction().replace(R.id.container, StudentAssessmentListFragment()).addToBackStack(null).commitAllowingStateLoss()
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.longClickedStudentSnapshot.collect {
                MaterialAlertDialogBuilder(MainActivity2.activityContext!!).setBackground(MainActivity2.activityContext!!.getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which -> }.setPositiveButton("yes") { dialog, which -> deleteData(it) }.show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.deleteStudentAction.collect {

                it.reference.delete().addOnSuccessListener {
                    Log.d(TAG, "deleteData: deletion success")
                }
            }
        }


    }

    private fun setData(list: List<DocumentSnapshot>?) {
        Log.d(TAG, "setData: list size:${list?.size}")
        adapter.submitList(list)
    }

    private fun showProgressBar(visible: Boolean) {
        binding.progressBar.isVisible = visible
    }


    private fun setGestureListener() {
        binding.recyclerview.setOnTouchListener(SwipeGestureListener(this))
    }

    private fun setOnClickListeners() {
        binding.fob.setOnClickListener {
            addstudent()
        }


    }


    private fun showTabs(visible: Boolean) {
        binding.tabs.isVisible = visible
    }

    private fun searchViewIsEmpty() {
        Log.d(TAG, "searchViewIsEmpty: ${tabs.selectedTabPosition}")

    }

    private fun onStudentLongClicked(it: DocumentSnapshot) {
        Log.d(TAG, "onStudentLongClicked: ")

        viewModel.setEvent(Event.StudentLongClicked(it))

    }

    private fun deleteData(it: DocumentSnapshot) {

        viewModel.setEvent(Event.DeleteStudent(it))


    }


    private fun databaseIsEmpty() {
        Log.d(TAG, "databaseIsEmpty: ")
        if (context != null) {
            MaterialAlertDialogBuilder(MainActivity2.activityContext!!).setBackground(MainActivity2.activityContext?.getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_add_24).setTitle("Add").setMessage("Do You want To add Student ").setNegativeButton("no") { dialog, which ->
            }.setPositiveButton("yes") { dialog, which -> addstudent() }.show()

        }


    }

    private fun onStudentClicked(it: DocumentSnapshot) {

        viewModel.setEvent(Event.StudentClicked(it))
        studentDocumentSnapshot = it
        Log.d(TAG, "onStudentClicked: student Has been clicked")
    }


    private fun setUpTabLayout() {
        binding.tabs.addTab(binding.tabs.newTab().setText("UNKNOWN"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Beginner"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Letter"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Word"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Paragraph"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Story"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Above"))


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
                viewModel.setEvent(Event.StudentLearningLevelQuery(Learning_Level.UNKNOWN.name))
            }
            1 -> {
                viewModel.setEvent(Event.StudentLearningLevelQuery(Learning_Level.BEGINNER.name))
            }
            2 -> {
                viewModel.setEvent(Event.StudentLearningLevelQuery(Learning_Level.LETTER.name))
            }
            3 -> {
                viewModel.setEvent(Event.StudentLearningLevelQuery(Learning_Level.WORD.name))
            }
            4 -> {
                viewModel.setEvent(Event.StudentLearningLevelQuery(Learning_Level.PARAGRAPH.name))
            }
            5 -> {
                viewModel.setEvent(Event.StudentLearningLevelQuery(Learning_Level.STORY.name))
            }
            6 -> {
                viewModel.setEvent(Event.StudentLearningLevelQuery(Learning_Level.ABOVE.name))
            }
        }


    }

    private fun setUpToolbar() {
        Log.d(TAG, "setUpToolbar: ")
        binding.toolbar.root.inflateMenu(R.menu.learning_level_menu)
        binding.toolbar.root.setTitle("Grouping")
        binding.toolbar.root.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                R.id.addStudentItem -> {
                    addstudent()
                }
            }


            true
        }
        val searchItem = (binding.toolbar.root.menu.findItem(R.id.searchItem))
        val searchView = searchItem.actionView as SearchView

        MenuItemCompat.setOnActionExpandListener(searchItem, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {

                viewModel.setEvent(Event.SearchActive)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.setEvent(Event.SearchNotActive)
                return true
            }
        })

        searchView.onQueryTextChanged { newText ->

            if (newText.isNotEmpty()) {
                Log.d(TAG, "onQueryTextChange:  newText:$newText")
                viewModel.setEvent(Event.SearchActive)
                viewModel.setEvent(Event.StudentQuery(newText?.toLowerCase()))
            } else {
                Log.d(TAG, "onQueryTextChange: no text")
            }

        }

    }

    fun addstudent() {

        val myIntent = Intent(MainActivity2.activityContext!!, AddStudentFragment::class.java)
        startActivity(myIntent)
    }

    override fun onSwipeLeft() {
        Log.d(TAG, "onSwipeLeft: ")
        Log.d(TAG, "onSwipeLeft: selectedTabPosition:${tabs.selectedTabPosition} : :tab size:${tabs.tabCount}")
        viewModel.setEvent(Event.SwipeLeft(tabs.selectedTabPosition, tabs.tabCount))


    }

    override fun onSwipeRight() {
        Log.d(TAG, "onSwipeRight: ")
        Log.d(TAG, "onSwipeRight: selectedTabPosition:${tabs.selectedTabPosition} : :tab size:${tabs.tabCount}")
        viewModel.setEvent(Event.SwipeRight(tabs.selectedTabPosition, tabs.tabCount))
    }

    fun swipeScreen(position: Int) {
        Log.d(TAG, "onSwipeLeft: ")
        Log.d(TAG, "onSwipeLeft: selectedTabPosition:${tabs.selectedTabPosition} : :tab size:${tabs.tabCount}")
        tabs.getTabAt(position)!!.select()
        Log.d(TAG, "onSwipeLeft: position:$position")
    }

    override fun onStop() {
        super.onStop()
        if (this::listenerRegistration.isInitialized) {
            listenerRegistration.remove()
        }
    }


    sealed class Event {
        data class OnStudentClicked<T>(val studentDocumentSnapshot: T) : Event()
        data class OnStudentLongClicked<T>(val studentDocumentSnapshot: T) : Event()
        data class StudentQuery(val query: String) : Event()
        data class StudentLearningLevelQuery(val query: String) : Event()
        object SearchActive : Event()
        object SearchNotActive : Event()
        data class SwipeLeft(val selectedTabPosition: Int, val tabCount: Int) : Event()
        data class SwipeRight(val selectedTabPosition: Int, val tabCount: Int) : Event()
        data class StudentClicked(val studentSnapshot: DocumentSnapshot) : Event()
        data class StudentLongClicked(val studentSnapshot: DocumentSnapshot) : Event()
        data class DeleteStudent(val studentSnapshot: DocumentSnapshot) : Event()
    }
}
