package com.example.edward.nyansapo.numeracy.numeracy_assessment_result.results_list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ActivityStudentInfoPage2Binding
import com.example.edward.nyansapo.*
import com.edward.nyansapo.databinding.ActivityStudentInfoPageBinding
import com.example.edward.nyansapo.numeracy.AssessmentNumeracy
import com.example.edward.nyansapo.numeracy.Numeracy_Learning_Levels
import com.example.edward.nyansapo.numeracy.numeracy_assessment_result.NumeracyAssessmentResultViewModel
import com.example.edward.nyansapo.presentation.ui.main.MainActivity2
import com.example.edward.nyansapo.select_assessment.SelectAssessment
import com.example.edward.nyansapo.util.*
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class StudentAssessmentListFragment : Fragment(R.layout.activity_student_info_page2) {

    private val TAG = "StudentInfoPageFragment"

    lateinit var binding: ActivityStudentInfoPage2Binding
    lateinit var assessMentAdapter: StudentAssessmentAdapter2
    private val navArgs: StudentAssessmentListFragmentArgs by navArgs()
    private val viewModel: StudentAssessmentListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: student:${navArgs.student}")
        binding = ActivityStudentInfoPage2Binding.bind(view)
        setUpToolbar()
        initRecyclerViewAdapter()
        setSwipeListenerForItems()
        viewModel.setEvent(StudentAssessmentListViewModel.Event.FetchAssessments(navArgs.student))
        subscribeToObservers()

    }


    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.fetchAssessmentsStatus.collect {
                    Log.d(TAG, "subscribeToObservers:fetchAssessmentsStatus:${it.status.name} ")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                            viewModel.saveAssessMents(it.data!!)
                            setUpGraph(it.data!!.map { it.assessmentNumeracy })
                            submitList(it.data!!)
                            setSwipeListenerForItems()

                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                            Log.d(TAG, "subscribeToObservers: error:${it.exception!!.message}")


                        }
                        Resource.Status.EMPTY -> {
                            showProgress(false)
                            showToastInfo("No Assessment has been done")


                        }
                    }
                }
            }
        }
    }

    private fun submitList(data: List<DocumentSnapshot>) {

        assessMentAdapter.submitList(data)
    }

    private fun showProgress(visible: Boolean) {
        requireContext().showProgress(visible)
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    fun getLevelIndex(level: String?): Int {
        return when (level) {
            Numeracy_Learning_Levels.UNKNOWN.name -> 0
            Numeracy_Learning_Levels.BEGINNER.name -> 1
            Numeracy_Learning_Levels.ADDITION.name -> 2
            Numeracy_Learning_Levels.SUBTRACTION.name -> 3
            Numeracy_Learning_Levels.MULTIPLICATION.name -> 4
            Numeracy_Learning_Levels.DIVISION.name -> 5
            Numeracy_Learning_Levels.ABOVE.name -> 6
            else -> -1
        }
    }

    private fun setUpGraph(assessmentList: List<AssessmentNumeracy>) {


        if (assessmentList!!.size > 0) {
            //Toast.makeText(this, assessmentList.get(assessmentList.size()-1).getLEARNING_LEVEL(),Toast.LENGTH_LONG).show();
            val series = LineGraphSeries<DataPoint>()
            val num = assessmentList!!.size
            var i = 0
            while (i < num && i < 5) {
                series.appendData(DataPoint((i + 1).toDouble(), getLevelIndex(assessmentList!!.get(i).learningLevelNumeracy).toDouble()), true, 5)
                i++
            }
            series.setAnimated(true)

            binding.graphview.apply {
                addSeries(series)
                title = "Literacy Level Vs. Time of Current Assessments"

                viewport.isScalable = true
                viewport.isScrollable = true
                viewport.setScalableY(true)
                gridLabelRenderer.horizontalAxisTitle = "Time of Current Assessments"
                gridLabelRenderer.verticalAxisTitle = "Literacy Level"

                viewport.isXAxisBoundsManual = true
                viewport.setMinX(1.0)
                viewport.setMaxX(5.0)
                viewport.isYAxisBoundsManual = true
                viewport.setMinY(0.0)
                viewport.setMaxY(4.0)
                gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
                    override fun formatLabel(value: Double, isValueX: Boolean): String {
                        return if (!isValueX) {
                            when (value.toInt()) {
                                0 -> "U"
                                1 -> "B"
                                2 -> "A"
                                3 -> "S"
                                4 -> "M"
                                5 -> "D"
                                6 -> "A"
                                else -> "U"
                            }
                        } else super.formatLabel(value, isValueX)
                    }
                }
            }

        } else {

            val series = LineGraphSeries(arrayOf<DataPoint>(
                    DataPoint(0.toDouble(), 0.toDouble()),
                    DataPoint(1.toDouble(), 0.toDouble()),
                    DataPoint(2.toDouble(), 0.toDouble()),
                    DataPoint(3.toDouble(), 0.toDouble()),
                    DataPoint(4.toDouble(), 0.toDouble())))
            series.setAnimated(true) // set animation
            binding.graphview.apply {
                addSeries(series)
                title = "No Assessment has been recorded"
                viewport.isXAxisBoundsManual = true
                viewport.setMinX(1.0)
                viewport.setMaxX(5.0)
                viewport.isYAxisBoundsManual = true
                viewport.setMinY(0.0)
                viewport.setMaxY(4.0)
                gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
                    override fun formatLabel(value: Double, isValueX: Boolean): String {
                        return if (!isValueX) {
                            when (value.toInt()) {
                                0 -> "L"
                                1 -> "W"
                                2 -> "P"
                                3 -> "S"
                                4 -> "A"
                                else -> "U"
                            }
                        } else super.formatLabel(value, isValueX)
                    }
                }


            }

        }
    }


    private fun initRecyclerViewAdapter() {


        assessMentAdapter = StudentAssessmentAdapter2 {
            onAssmentClicked(it)
        }
        binding.recyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = assessMentAdapter
        }

    }


    private fun setSwipeListenerForItems() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //  assessMentAdapter?.deleteFromDatabase(viewHolder.adapterPosition)
                Log.d(TAG, "onSwiped: ")
            }
        }).attachToRecyclerView(recyclerview)
    }

    fun onAssmentClicked(snapshot: DocumentSnapshot) {
        Log.d(TAG, "onAssmentClicked: ${snapshot.assessmentNumeracy}")

    val choosen=viewModel.assessMentsListFlow.value.indexOf(snapshot)
        findNavController().navigate(StudentAssessmentListFragmentDirections.actionStudentAssessmentListFragmentToNumeracyAssessmentResultFragment(navArgs.student,choosen))
    }


    fun openDialog() {

        FirebaseUtils.showAlertDialog(MainActivity2.activityContext!!, R.drawable.button_first, "Add Assessment", "Do you want to add an Assessment?", {
            addAssessment()
        }) {
            //no clicked

        }

    }

    private fun addAssessment() {

        Log.d(TAG, "addAssessment: btn clicked ")
        val intent = Intent(MainActivity2.activityContext!!, SelectAssessment::class.java)
        startActivity(intent)
    }

    private fun setUpToolbar() {
        binding.toolbar.root.inflateMenu(R.menu.student_menu)

        binding.toolbar.root.title = navArgs.student.firstname
        binding.toolbar.root.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbar.root.setOnMenuItemClickListener { item ->

            when (item.itemId) {
                R.id.settings -> {
                    val myIntent = Intent(MainActivity2.activityContext!!, studentSettings::class.java)
                    startActivity(myIntent)
                    true
                }
                R.id.add_assessment -> {
                    addAssessment()
                    true
                }
                R.id.analytics -> {
                    val intent = Intent(MainActivity2.activityContext!!, studentDetails::class.java)
                    startActivity(intent)
                    true
                }

            }



            true
        }
    }
}