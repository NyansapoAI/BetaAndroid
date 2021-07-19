package com.example.edward.nyansapo.presentation.ui.attendance

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ActivityAttendanceBinding
import com.edward.nyansapo.databinding.ItemAttendanceBinding
import com.example.edward.nyansapo.presentation.ui.add_student.AddStudentFragment
import com.example.edward.nyansapo.presentation.ui.attendance.AttendanceViewModel.Event
import com.example.edward.nyansapo.presentation.ui.main.MainActivity2
import com.example.edward.nyansapo.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class AttendanceFragment : Fragment(R.layout.activity_attendance) {
    private val TAG = "AttendanceFragment"
    lateinit var binding: ActivityAttendanceBinding
    lateinit var attendanceAdapter: AttendanceAdapter2
    private val viewModel: AttendanceViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        binding = ActivityAttendanceBinding.bind(view)
        initProgressBar()
        setUpToolBar()
        setLabel(Calendar.getInstance().time.formatDate)
        setOnClickListeners()
        initRecyclerViewAdapter()
        setSwipeListenerForItems()
        val currentDate = Calendar.getInstance().time
        initDataFetching(currentDate)
        subScribeToObservers()
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.attendanceEvents.collect {
                    when (it) {
                        is Event.CorrectDateChoosen -> {
                            initDataFetching(it.date)
                        }
                        is Event.FutureDateChoosen -> {
                            futureDateChoosen()
                        }
                    }
                }
            }
            launch {
                viewModel.dataFetchingStatus.collect {
                    Log.d(TAG, "subScribeToObservers: dataFetchingStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)
                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                            submitList(it.data!!)
                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                            showToastInfo(it.exception!!.message!!)
                        }
                    }
                }
            }
            launch {
                viewModel.queryStatus.collect {
                    submitList(it)
                }
            }
        }
    }

    private fun submitList(data: List<DocumentSnapshot>) {
        attendanceAdapter.submitList(data)

    }

    private fun futureDateChoosen() {
        showToastInfo("Please do not choose future date!!")
    }

    private fun initDataFetching(date: Date) {
        setLabel(date.formatDate)
        viewModel.setEvent(Event.InitDataFetching(date))
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun setOnClickListeners() {

        binding.dateBtn.setOnClickListener {
            dateBtnClicked()
        }

        binding.submitBtn.setOnClickListener {
            submitBtnClicked()
        }
    }

    private fun dateBtnClicked() {
        val myCalendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar[Calendar.YEAR] = year
            myCalendar[Calendar.MONTH] = monthOfYear
            myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
            viewModel.setEvent(Event.CorrectDateChoosen(myCalendar.time))
        }


        DatePickerDialog(requireContext(), dateSetListener, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]).show()


    }


    private fun submitBtnClicked() {
        if (this::attendanceAdapter.isInitialized) {
            binding.submitBtn.isVisible = false
            //set edit item to visible
            binding.toolbar.root.menu.findItem(R.id.editItem).isVisible = true
            weWantToChangeIfTheCheckBoxIsEnabledOrDisabledInTheRecylerView(false)

        } else {
            Log.d(TAG, "submitBtnClicked: adapter not initialized maybe becos database is empty")
            Toasty.info(requireContext(), "No Data In Database").show()
        }

    }

    private fun weWantToChangeIfTheCheckBoxIsEnabledOrDisabledInTheRecylerView(enabled: Boolean) {
        if (attendanceAdapter != null) {
            Log.d(TAG, "weWantToChangeIfTheCheckBoxIsEnabledOrDisabledInTheRecylerView: started updading checkboxes")
            for (position in 0 until binding.recyclerview.adapter!!.itemCount) {
                Log.d(TAG, "weWantToChangeIfTheCheckBoxIsEnabledOrDisabledInTheRecylerView: $position updated")
                val binding = ItemAttendanceBinding.bind(binding.recyclerview.getChildAt(position))
                binding.attendanceCheckbox.isEnabled = enabled
                attendanceAdapter.notifyDataSetChanged()


            }
        } else {
            Toasty.info(requireContext(), "Adapter is null").show()
            Log.d(TAG, "recreateAdapterWithCheckBoxesDisabled: adapter is null")
        }

    }

    private fun setUpToolBar() {
        binding.toolbar.root.inflateMenu(R.menu.attendance_menu)
        binding.toolbar.root.title = "Attendance"
        binding.toolbar.root.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.toolbar.root.setOnMenuItemClickListener { item ->

            when (item.itemId) {
                R.id.editItem -> {
                    editItemClicked(item)
                }


            }
            true

        }

        val searchView = (binding.toolbar.root.menu.findItem(R.id.searchItem).actionView as SearchView)
        searchView.onQueryTextChanged {query->
            Log.d(TAG, "setUpToolBar: query:$query")
            viewModel.setEvent(Event.StartQuery(query))
        }


    }

    private fun editItemClicked(item: MenuItem) {
        binding.submitBtn.isVisible = true
        item.isVisible = false
        weWantToChangeIfTheCheckBoxIsEnabledOrDisabledInTheRecylerView(true)
    }


    private fun goToAddStudent() {
        val myIntent = Intent(requireContext(), AddStudentFragment::class.java)
        startActivity(myIntent)

    }

    private fun initRecyclerViewAdapter() {
        attendanceAdapter = AttendanceAdapter2 { snapshot, isChecked ->
            onCheckBoxClicked(snapshot, isChecked)
        }
        binding.recyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = attendanceAdapter
        }
    }


    private fun setSwipeListenerForItems() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteFromDatabase(viewHolder.bindingAdapterPosition)
            }
        }).attachToRecyclerView(recyclerview)
    }

    private fun deleteFromDatabase(position: Int) {
        attendanceAdapter.currentList.get(position).reference.delete().addOnSuccessListener {
            Log.d(TAG, "deleteFromDatabase: success deleting attendance")
        }
    }

    private fun onCheckBoxClicked(documentSnapshot: DocumentSnapshot, ischecked: Boolean) {
        Log.d(TAG, "onCheckBoxClicked: started updating attendance")
        val map = mapOf("present" to ischecked)
        documentSnapshot.reference.set(map, SetOptions.merge()).addOnSuccessListener {
            Log.d(TAG, "onCheckBoxClicked: success updating attendance")
        }
    }


    private fun setLabel(date: String) {
        binding.dateBtn.text = date

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