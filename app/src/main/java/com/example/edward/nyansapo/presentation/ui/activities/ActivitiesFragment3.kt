package com.example.edward.nyansapo.presentation.ui.activities

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentActivities3Binding
import com.example.edward.nyansapo.numeracy.numeracy_learning_level.Data
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
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
        initProgressBar()
        initRecyclerViewAdapter()
        subScribeToObservers()
        setUpToolbar()

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
        searchView.onQueryTextChanged { query ->
            Log.d(TAG, "setUpToolbar: query:$query")

            viewModel.setEvent(ActivitiesViewModel.Event.StartQuery(query))
        }
    }

    private fun initRecyclerViewAdapter() {
        activitiesAdapter = ActivitiesAdapter3 { onActivityClicked(it) }
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

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getActivitiesStatus.collect {
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)

                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                            showToastInfo(it.exception!!.message!!)
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

    /////////////////////PROGRESS_BAR////////////////////////////
    lateinit var dialog: AlertDialog

    private fun showProgress(show: Boolean) {

        if (show) {
            dialog.show()

        } else {
            dialog.dismiss()

        }

    }

    private fun initProgressBar() {

        dialog = setProgressDialog(requireContext(), "Loading..")
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
    }

    fun setProgressDialog(context: Context, message: String): AlertDialog {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = message
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20.toFloat()
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }

    //end progressbar
}
