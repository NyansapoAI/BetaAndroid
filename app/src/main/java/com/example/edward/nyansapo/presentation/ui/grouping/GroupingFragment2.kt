package com.example.edward.nyansapo.presentation.ui.grouping

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentLearningLevelBinding
import com.example.edward.nyansapo.AddStudentFragment
import com.example.edward.nyansapo.Learning_Level
import com.example.edward.nyansapo.presentation.ui.main.MainActivity2
import com.example.edward.nyansapo.presentation.ui.student.StudentAssessmentListFragment
import com.example.edward.nyansapo.presentation.utils.Constants
import com.example.edward.nyansapo.presentation.utils.FirebaseUtils
import com.example.edward.nyansapo.presentation.utils.studentDocumentSnapshot
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_learning_level.*

const val REQUEST_CODE: Int=7
class GroupingFragment2 : Fragment(R.layout.fragment_learning_level), SwipeListener {


    private val TAG = "LearningLevelFragment"
    lateinit var adapter: GroupingAdapter2
    lateinit var listenerRegistration: ListenerRegistration
    lateinit var originalList: MutableList<DocumentSnapshot>




    lateinit var binding: FragmentLearningLevelBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLearningLevelBinding.bind(view)
        setUpToolbar()
        setUpTabLayout()
        setOnClickListeners()
        setGestureListener()




        checkIfTheDatabaseIsEmpty()
        val searchView = (binding.toolbar.root.menu.findItem(R.id.searchItem).actionView as SearchView)

        initRecyclerViewAdapter()


    }

    override fun onResume() {
        super.onResume()
        fetchAllStudents()
    }

    private fun fetchAllStudents(onComplete: (List<DocumentSnapshot>) -> Unit = {}) {
        Log.d(TAG, "fetchAllStudents: ")
        val sharedPreferences = MainActivity2.activityContext!!.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)

        val programId = sharedPreferences.getString(Constants.KEY_PROGRAM_ID, null)
        val groupId = sharedPreferences.getString(Constants.KEY_GROUP_ID, null)
        val campId = sharedPreferences.getString(Constants.KEY_CAMP_ID, null)
        val campPos = sharedPreferences.getInt(Constants.CAMP_POS, -1)

        Log.d(TAG, "initRecyclerViewAdapter: programid $programId  groupid $groupId campid $campId}")

        if (campPos == -1) {
            Toasty.error(MainActivity2.activityContext!!, "Please First create A camp before coming to this page", Toasty.LENGTH_LONG).show()
            MainActivity2.activityContext!!.supportFragmentManager.popBackStackImmediate()
        }


        val query: Query = FirebaseUtils.getCollectionStudentFromCamp_ReturnCollection(programId, groupId, campId)
        listenerRegistration = query.addSnapshotListener { querySnapshot, firebaseException ->

            originalList = querySnapshot!!.documents

            onComplete(originalList)

        }

    }

    private fun setGestureListener() {
        binding.recyclerview.setOnTouchListener(SwipeGestureListener(this))
    }

    private fun setOnClickListeners() {
        binding.fob.setOnClickListener {
            addstudent()
        }


    }


    private fun initRecyclerViewAdapter(learningLevel: String = Learning_Level.UNKNOWN.name) {
        Log.d(TAG, "initRecyclerViewAdapter: ")

        val sharedPreferences = MainActivity2.activityContext!!.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)

        val programId = sharedPreferences.getString(Constants.KEY_PROGRAM_ID, null)
        val groupId = sharedPreferences.getString(Constants.KEY_GROUP_ID, null)
        val campId = sharedPreferences.getString(Constants.KEY_CAMP_ID, null)
        val campPos = sharedPreferences.getInt(Constants.CAMP_POS, -1)

        Log.d(TAG, "initRecyclerViewAdapter: programid $programId  groupid $groupId campid $campId}")

        if (campPos == -1) {
            Toasty.error(MainActivity2.activityContext!!, "Please First create A camp before coming to this page", Toasty.LENGTH_LONG).show()
            MainActivity2.activityContext!!.supportFragmentManager.popBackStackImmediate()
        }

        adapter = GroupingAdapter2(this, {
            searchViewIsEmpty()
        },
                {
                    onStudentClicked(it)
                }, {
            onStudentLongClicked(it)
        })

        val query: Query = FirebaseUtils.getCollectionStudentFromCamp_ReturnCollection(programId, groupId, campId).whereEqualTo("learningLevel", learningLevel)


        query.get().addOnSuccessListener {
            adapter.submitList(it.documents)
        }


        binding.recyclerview.setLayoutManager(LinearLayoutManager(MainActivity2.activityContext!!))
        binding.recyclerview.setAdapter(adapter)
        Log.d(TAG, "initRecyclerViewAdapter: adapter set up")


    }

    private fun showTabs(visible: Boolean) {
        binding.tabs.isVisible = visible
    }

    private fun searchViewIsEmpty() {
        Log.d(TAG, "searchViewIsEmpty: ${tabs.selectedTabPosition}")

    }

    private fun onStudentLongClicked(it: DocumentSnapshot) {
        MaterialAlertDialogBuilder(MainActivity2.activityContext!!).setBackground(MainActivity2.activityContext!!.getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which -> }.setPositiveButton("yes") { dialog, which -> deleteData(it) }.show()

    }

    private fun deleteData(it: DocumentSnapshot) {
        it.reference.delete().addOnSuccessListener {
            Log.d(TAG, "deleteData: deletion success")
        }

    }

    private fun checkIfTheDatabaseIsEmpty() {
        val sharedPreferences = MainActivity2.activityContext!!.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)

        val programId = sharedPreferences.getString(Constants.KEY_PROGRAM_ID, null)
        val groupId = sharedPreferences.getString(Constants.KEY_GROUP_ID, null)
        val campId = sharedPreferences.getString(Constants.KEY_CAMP_ID, null)
        val campPos = sharedPreferences.getInt(Constants.CAMP_POS, -1)

        if (campPos == -1) {
            Toasty.error(MainActivity2.activityContext!!, "Please First create A camp before coming to this page", Toasty.LENGTH_LONG).show()
            MainActivity2.activityContext!!.supportFragmentManager.popBackStackImmediate()
        }

        FirebaseUtils.getCollectionStudentFromCamp_ReturnSnapshot(programId, groupId, campId) {

            if (it.isEmpty) {
                Log.d(TAG, "checkIfTheDatabaseIsEmpty: database is empty")

                if (context != null) {
                    Log.d(TAG, "checkIfTheDatabaseIsEmpty: context is null")
                    MaterialAlertDialogBuilder(MainActivity2.activityContext!!).setBackground(MainActivity2.activityContext?.getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_add_24).setTitle("Add").setMessage("Do You want To add Student ").setNegativeButton("no") { dialog, which ->
                    }.setPositiveButton("yes") { dialog, which -> addstudent() }.show()

                }

            } else {
                Log.d(TAG, "checkIfTheDatabaseIsEmpty: database has ${it.size()} students")
            }
        }
    }

    private fun onStudentClicked(it: DocumentSnapshot) {
        studentDocumentSnapshot = it
        Log.d(TAG, "onStudentClicked: student Has been clicked")
        MainActivity2.activityContext!!.supportFragmentManager.beginTransaction().replace(R.id.container, StudentAssessmentListFragment()).addToBackStack(null).commitAllowingStateLoss()
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
                initRecyclerViewAdapter(Learning_Level.UNKNOWN.name)
            }
            1 -> {
                initRecyclerViewAdapter(Learning_Level.BEGINNER.name)
            }
            2 -> {
                initRecyclerViewAdapter(Learning_Level.LETTER.name)
            }
            3 -> {
                initRecyclerViewAdapter(Learning_Level.WORD.name)
            }
            4 -> {
                initRecyclerViewAdapter(Learning_Level.PARAGRAPH.name)
            }
            5 -> {
                initRecyclerViewAdapter(Learning_Level.STORY.name)
            }
            6 -> {
                initRecyclerViewAdapter(Learning_Level.ABOVE.name)
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
        val searchView = (binding.toolbar.root.menu.findItem(R.id.searchItem).actionView as SearchView)

         searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                if (newText != null && newText.isNotEmpty()) {
                    Log.d(TAG, "onQueryTextChange:  newText:$newText")
                    showTabs(false)
                    adapter.filter.filter(newText?.toLowerCase())
                } else {
                    Log.d(TAG, "onQueryTextChange: no text")
                    showTabs(true)
                    initRecyclerViewAdapter()
                }
                return true
            }
        })
    }


    fun addstudent() {

        val myIntent = Intent(MainActivity2.activityContext!!, AddStudentFragment::class.java)
        startActivity(myIntent)
    }

    override fun onSwipeLeft() {
        Log.d(TAG, "onSwipeLeft: ")
        Log.d(TAG, "onSwipeLeft: selectedTabPosition:${tabs.selectedTabPosition} : :tab size:${tabs.tabCount}")

        val position = (tabs.selectedTabPosition + 1) % tabs.tabCount
        tabs.getTabAt(position)!!.select()
        Log.d(TAG, "onSwipeLeft: position:$position")
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

    override fun onStop() {
        super.onStop()
        if (this::listenerRegistration.isInitialized) {
            listenerRegistration.remove()
        }
    }
}