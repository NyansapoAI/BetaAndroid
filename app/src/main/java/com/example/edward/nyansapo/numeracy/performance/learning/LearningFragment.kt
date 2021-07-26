package com.example.edward.nyansapo.numeracy.performance.learning

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentLearningBinding
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.presentation.ui.change_program.Camp
import com.example.edward.nyansapo.presentation.ui.change_program.SpinnerAdapter
import com.example.edward.nyansapo.presentation.ui.main.campId
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.showProgress
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LearningFragment : Fragment(R.layout.fragment_learning) {

    private val TAG = "LearningFragment"

    @Inject
    lateinit var sharedPref: SharedPreferences

    lateinit var campNames: QuerySnapshot
    private val viewModel: LearningViewModel by viewModels()

    private lateinit var binding: FragmentLearningBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLearningBinding.bind(view)
        setItemClickListeners()
        subScribeToObservers()
    }

    var groupCheck = 0
    private fun setItemClickListeners() {
        binding.campSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {

                Log.d(TAG, "onItemSelected: camp spinner current pos: ${binding.campSpinner.selectedItemPosition}")
                if (++groupCheck > 1) {
                    //saving campId to be accessed in other screens
                    campSelected()
                }

            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                Log.d(TAG, "onNothingSelected: ")

            }
        })

    }

    private fun campSelected() {
        val camp = binding.campSpinner.selectedCamp?.toObject(Camp::class.java)
        val campId = binding.campSpinner.selectedCamp?.id
        val campPos = binding.campSpinner.selectedItemPosition
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.campStatus.collect {
                    Log.d(TAG, "subscribeToObservers: status:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            //show progress bar
                            showProgress(true)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                            val camps = it.data
                            campNames = camps!!

                            val spinnerValue = camps.map {
                                "Camp: ${it.toObject(Camp::class.java).number}"

                            }
                            val adapter = SpinnerAdapter(requireContext()!!, campNames!!, spinnerValue, { deleteItem(it) }) { documentReference, documentSnapshot ->
                                val camp = documentSnapshot.toObject(Camp::class.java)

                            }
                            binding.campSpinner.setAdapter(adapter)
                        }
                        Resource.Status.EMPTY -> {
                            showProgress(false)
                            Log.d(TAG, "subScribeToObservers: camps is empty")

                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                            showToastInfo(it.exception?.message!!)
                        }
                    }
                }
            }

            launch {
                viewModel.fetchStudentsStatus.collect {
                    Log.d(TAG, "fetchStudentsStatus: status:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)
                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)

                        }
                        Resource.Status.EMPTY -> {
                            showProgress(false)
                            showToastInfo("Student database is empty!!")

                        }
                    }
                }
            }

            launch {
                viewModel.computeGraphDataStatus.collect {
                    setGraphData(it)
                }
            }
        }
    }

    private fun setGraphData(graphData: LearningViewModel.GraphData) {


        setNumberOfStudents(graphData.totalCount)

        //sortStudents(students)

        val graph = binding.cumulativeGraph
        val series = BarGraphSeries(arrayOf<DataPoint>(
                DataPoint(1.toDouble(), graphData.beginnerCount.toDouble()),
                DataPoint(2.toDouble(), graphData.additionCount.toDouble()),
                DataPoint(3.toDouble(), graphData.subtractionCount.toDouble()),
                DataPoint(4.toDouble(), graphData.multiplicationCount.toDouble()),
                DataPoint(5.toDouble(), graphData.divisionCount.toDouble()),
                DataPoint(6.toDouble(), graphData.aboveCount.toDouble())
                //DataPoint(5.toDouble(), (students as ArrayList<Student>).size.toDouble())
        ))

        //set spacing between bars
        series.spacing = 10
        series.isAnimated = true
        graph.addSeries(series)
        graph.title = "Students Vs.  Numeracy Literacy Level"

        graph.gridLabelRenderer.horizontalAxisTitle = "Numeracy Literacy Level"
        graph.gridLabelRenderer.verticalAxisTitle = "Students"

        //graph.viewport.isScalable = true
        ///graph.viewport.isScrollable = true
        //graph.viewport.setScalableY(true)


        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinX(0.5)
        graph.viewport.setMaxX(6.5)
        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.setMinY(0.0)
        //graph.viewport.setMaxY((students as ArrayList<Student>).size.toDouble())
        graph.viewport.setMaxY(getMaxY(graphData)) // get Maximum Y dynamically from the data
        graph.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.HORIZONTAL
        graph.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    when (value.toInt()) {
                        1 -> "Beginner"
                        2 -> "Addition"
                        3 -> "Subtraction"
                        4 -> "Multiplication"
                        5 -> "Division"
                        6 -> "Above"
                        else -> "U"
                    }
                } else super.formatLabel(value, isValueX)
            }
        }
        try {
           // setMissedWords()
        } catch (e: Exception) {
            //there is possibility of user closing this screen while am still trying to get the assessment list hence the MainActivity2.activityContext!! will return null
            e.printStackTrace()
        }


    }

    private fun getMaxY(graphData: LearningViewModel.GraphData): Double {
        graphData.apply {
            val max = listOf(beginnerCount, additionCount, subtractionCount, multiplicationCount, divisionCount, aboveCount).maxOrNull()!!
            return max + 1.0

        }
    }

    private fun setNumberOfStudents(size: Int) {
        binding.totalStudentsTxtView.text = "$size \n Students"
    }

    private fun deleteItem(it: DocumentReference) {
        Log.d(TAG, "deleteItem: ")
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun showProgress(visible: Boolean) {
        requireContext().showProgress(visible)
    }

    val Spinner.selectedCamp: DocumentSnapshot?
        get() {
            if (binding.campSpinner.selectedItemPosition == -1) {
                return null
            } else {
                return campNames.documents[binding.campSpinner.selectedItemPosition]
            }
        }
}