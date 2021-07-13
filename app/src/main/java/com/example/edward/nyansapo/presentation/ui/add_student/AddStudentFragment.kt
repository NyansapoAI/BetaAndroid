package com.example.edward.nyansapo.presentation.ui.add_student


import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentAddStudentBinding
import com.example.edward.nyansapo.Student

import com.example.edward.nyansapo.util.Constants
import com.example.edward.nyansapo.util.FirebaseUtils
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.studentDocumentSnapshot
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_index.*
import kotlinx.android.synthetic.main.activity_register_student.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddStudentFragment() : Fragment(R.layout.fragment_add_student) {

    private val TAG = "AddStudentFragment"

    private lateinit var binding: FragmentAddStudentBinding

    @Inject
    lateinit var sharePref: SharedPreferences
    private val viewModel: AddStudentViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddStudentBinding.bind(view)
        initProgressBar()
        setOnClickListener()
        subScribeToObservers()

    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.addStudentStatus.collect {
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
        }
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun setOnClickListener() {
        binding.btnRegister.setOnClickListener {
            btnRegisterClicked()

        }
    }

    private fun btnRegisterClicked() {
        val student = getStudentObject()
        Log.d(TAG, "btnRegisterClicked: student:$student")
        viewModel.setEvent(AddStudentViewModel.Event.AddStudent(student))
    }

    private fun getStudentObject(): Student {
        val student: Student
        student = Student()
        binding.apply {
            student.firstname = editFirstname!!.text.toString().trim()
            student.lastname = editLastname!!.text.toString().trim()
            student.age = editAge!!.text.toString().trim()// set age

            student.location = edtLocation!!.text.toString().trim()
            student.std_class = editClass!!.text.toString().trim()

            if (radioGroup.checkedRadioButtonId == R.id.maleRadioBtn) {
                student.gender = "Male"
            } else if (radioGroup.checkedRadioButtonId == R.id.femaleRadioBtn) {
                student.gender = "Female"
            } else {
                student.gender = "Other"
            }

        }
        return student
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
