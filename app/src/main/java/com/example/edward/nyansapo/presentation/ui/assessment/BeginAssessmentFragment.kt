package com.example.edward.nyansapo.presentation.ui.assessment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ActivityBeginAssessementBinding
import com.example.edward.nyansapo.Assessment
import com.example.edward.nyansapo.Student


import com.example.edward.nyansapo.presentation.ui.main.MainActivity2
import com.example.edward.nyansapo.util.Constants
import com.example.edward.nyansapo.util.FirebaseUtils
import com.example.edward.nyansapo.util.studentDocumentSnapshot
import com.example.edward.nyansapo.util.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class BeginAssessmentFragment : Fragment(R.layout.activity_begin_assessement) {


    private val TAG = "BeginAssessmentFragment"


    lateinit var binding: ActivityBeginAssessementBinding
    private val viewModel: BeginAssessmentViewModel by viewModels()
    private val navArgs: BeginAssessmentFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ActivityBeginAssessementBinding.bind(view)
        Log.d(TAG, "onViewCreated: student:${navArgs.student}")

        setUpToolbar()
        setOnClickListeners()
        subScribeToObservers()
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getStudent.collect {
                    Log.d(TAG, "subScribeToObservers: getStudent:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.SUCCESS -> {

                        }
                        Resource.Status.ERROR -> {
                            showToastInfo("Error:${it.exception?.message}")
                        }

                    }
                }
            }

            launch {
                viewModel.beginAssessmentEvents.collect {
                    when (it) {
                        is Event.BeginAssessmentClicked -> {
                            goToAvatarChooser(it.student)
                        }
                    }
                }
            }

            launch {
                viewModel.getAssessmentsStatus.collect {
                    Log.d(TAG, "subScribeToObservers: getAssessmentsStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.SUCCESS -> {
                            successGettingAssessments(it.data!!)

                        }
                        Resource.Status.EMPTY -> {
                            showToastInfo("No Assessment Available")

                        }
                        Resource.Status.ERROR -> {
                            showToastInfo("Error:${it.exception?.message}")
                        }
                    }
                }
            }
        }
    }

    private fun successGettingAssessments(data: List<DocumentSnapshot>) {
        val assessmentList = data.map { it.toObject(Assessment::class.java)!! }
        setUpGraph(assessmentList)

    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun setUpToolbar() {
        binding.toolbar.root.inflateMenu(R.menu.overflow_menu)
        val fullname = "${studentDocumentSnapshot!!.toObject(Student::class.java)!!.firstname}  ${studentDocumentSnapshot!!.toObject(Student::class.java)!!.lastname}"
        binding.toolbar.root.title = fullname
    }

    private fun setOnClickListeners() {
        binding.beginAssessmentBtn.setOnClickListener {
            viewModel.setEvent(Event.BeginAssessmentClicked(navArgs.student))
        }
    }

    private fun goToAvatarChooser(student: Student) {
        findNavController().navigate(BeginAssessmentFragmentDirections.actionBeginAssessmentFragmentToAvatarChooserFragment(student))
    }


    private fun setUpGraph(assessmentList: List<Assessment>) {


        // set onclick listeners
        if (assessmentList!!.size > 0) {
            Log.d(TAG, "setUpGraph: ")
            //Toast.makeText(this, assessmentList.get(assessmentList.size()-1).getLEARNING_LEVEL(),Toast.LENGTH_LONG).show();
            val graphView = binding.root.findViewById<View>(R.id.graphview) as GraphView
            val series = LineGraphSeries<DataPoint>()
            val num = assessmentList!!.size
            var i = 0
            while (i < num && i < 5) {
                Log.d(TAG, "setUpGraph: $i")
                series.appendData(DataPoint((i + 1).toDouble(), getLevelIndex(assessmentList!!.get(i).learningLevel).toDouble()), true, 5)
                i++
            }
            series.setAnimated(true)
            graphView!!.addSeries(series)
            graphView!!.title = "Literacy Level Vs. Time of Current Assessments"
            graphView!!.gridLabelRenderer.horizontalAxisTitle = "Time of Current Assessments"
            graphView!!.gridLabelRenderer.verticalAxisTitle = "Literacy Level"



            graphView!!.viewport.isScalable = true
            graphView!!.viewport.isScrollable = true
            graphView!!.viewport.setScalableY(true)



            graphView!!.viewport.isXAxisBoundsManual = true
            graphView!!.viewport.setMinX(1.0)
            graphView!!.viewport.setMaxX(5.0)
            graphView!!.viewport.isYAxisBoundsManual = true
            graphView!!.viewport.setMinY(0.0)
            graphView!!.viewport.setMaxY(4.0)
            graphView!!.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
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
        } else {
            val graphView = binding.root.findViewById<View>(R.id.graphview) as GraphView

            val series = LineGraphSeries(arrayOf<DataPoint>(
                    DataPoint(0.toDouble(), 0.toDouble()),
                    DataPoint(1.toDouble(), 0.toDouble()),
                    DataPoint(2.toDouble(), 0.toDouble()),
                    DataPoint(3.toDouble(), 0.toDouble()),
                    DataPoint(4.toDouble(), 0.toDouble())))
            series.setAnimated(true) // set animation
            graphView!!.addSeries(series)
            graphView!!.title = "No Assessment has been recorded"
            graphView!!.viewport.isXAxisBoundsManual = true
            graphView!!.viewport.setMinX(1.0)
            graphView!!.viewport.setMaxX(5.0)
            graphView!!.viewport.isYAxisBoundsManual = true
            graphView!!.viewport.setMinY(0.0)
            graphView!!.viewport.setMaxY(4.0)
            graphView!!.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
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


    fun getLevelIndex(level: String?): Int {
        return when (level) {
            "LETTER" -> 0
            "WORD" -> 1
            "PARAGRAPH" -> 2
            "STORY" -> 3
            "ABOVE" -> 4
            else -> -1
        }
    }

    sealed class Event {
        data class BeginAssessmentClicked(val student: Student) : Event()
        data class GetAssessments(val snapshot: DocumentSnapshot) : Event()

    }


}