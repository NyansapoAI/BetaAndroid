package com.example.edward.nyansapo.numeracy.numeracy_learning_level

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentNumeracyLearningLevelBinding
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.student
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NumeracyLearningLevelFragment : Fragment(R.layout.fragment_numeracy_learning_level) {

    private val TAG = "NumeracyLearningLevelFr"

    private val viewModel: NumeracyLearningLevelViewModel by viewModels()
    private lateinit var binding: FragmentNumeracyLearningLevelBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNumeracyLearningLevelBinding.bind(view)
        initProgressBar()
        initRecyclerViewAdapters()
        subScribeToObservers()
    }


    lateinit var levelSectionsAdapter: LevelSectionsAdapter
    private fun initRecyclerViewAdapters() {
        levelSectionsAdapter = LevelSectionsAdapter(requireContext()) { onStudentClicked(it) }
        binding.recyclerview.apply {
            setHasFixedSize(false)
            //  addItemDecoration(NumeracyItemDecoration())
            layoutManager = LinearLayoutManager(requireContext())
            adapter = levelSectionsAdapter

        }
        Data.setList()
          levelSectionsAdapter.submitList(Data.list)

    }

    private fun onStudentClicked(snapshot: DocumentSnapshot) {
        Log.d(TAG, "onStudentClicked: student:${snapshot.student}")
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getAllStudents.collect {
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)
                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)

                            viewModel.setEvent(NumeracyLearningLevelViewModel.Event.StartSortingData(it.data!!))

                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                            showToastInfo(it.exception!!.message!!)
                        }
                    }

                }
            }
            launch {
                viewModel.beginnerStudents.collect {
                    setBeginnerData(it)
                }
            }
            launch {
                viewModel.additionStudents.collect {
                    setAdditionData(it)
                }
            }
            launch {
                viewModel.subtractionStudents.collect {
                    setSubtractionData(it)
                }
            }
            launch {
                viewModel.multiplicationStudents.collect {
                    setMultiplicationData(it)
                }
            }
            launch {
                viewModel.divisionStudents.collect {
                    setDivisionData(it)
                }
            }
            launch {
                viewModel.aboveStudents.collect {
                    setAboveData(it)
                }
            }
        }
    }

    private fun setBeginnerData(it: List<DocumentSnapshot>) {
        Log.d(TAG, "setBeginnerData: size:${it.size}")
        if (it.size > 0) {
            val newList = Data.list
            newList[0].students = it.toMutableList()
            levelSectionsAdapter.submitList(newList)
        }
    }

    private fun setAdditionData(it: List<DocumentSnapshot>) {
        Log.d(TAG, "setAdditionData: size:${it.size}")
        if (it.size > 0) {
            val newList = Data.list
            newList[1].students = it.toMutableList()
            levelSectionsAdapter.submitList(newList)
        }
    }

    private fun setSubtractionData(it: List<DocumentSnapshot>) {
        Log.d(TAG, "setSubtractionData: size:${it.size}")
        if (it.size > 0) {
            val newList = Data.list
            newList[2].students = it.toMutableList()
            levelSectionsAdapter.submitList(newList)
        }
    }

    private fun setMultiplicationData(it: List<DocumentSnapshot>) {
        Log.d(TAG, "setMultiplicationData: size:${it.size}")
        if (it.size > 0) {
            val newList = Data.list
            newList[3].students = it.toMutableList()
            levelSectionsAdapter.submitList(newList)
        }
    }

    private fun setDivisionData(it: List<DocumentSnapshot>) {
        Log.d(TAG, "setDivisionData: size:${it.size}")
        if (it.size > 0) {
            val newList = Data.list
            newList[4].students = it.toMutableList()
            levelSectionsAdapter.submitList(newList)
        }
    }

    private fun setAboveData(it: List<DocumentSnapshot>) {
        Log.d(TAG, "setAboveData: size:${it.size}")
        if (it.size > 0) {
            val newList = Data.list
            newList[5].students = it.toMutableList()
            levelSectionsAdapter.submitList(newList)
        }
    }


    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
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