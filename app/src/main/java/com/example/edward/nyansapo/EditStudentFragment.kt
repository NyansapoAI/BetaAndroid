package com.example.edward.nyansapo


import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.*
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ActivityRegisterStudentBinding
import com.example.edward.nyansapo.presentation.utils.Constants
import com.example.edward.nyansapo.presentation.utils.studentDocumentSnapshot
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_register_student.*

class EditStudentFragment : AppCompatActivity() {


    private val TAG = "AddStudentFragment"

    lateinit var binding: ActivityRegisterStudentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initProgressBar()
        setUpToolbar()
        setOnClickListener()
        setUpDefaultValues()

    }

    private fun setUpDefaultValues() {
        if (studentDocumentSnapshot == null) {
            Toasty.error(this, "No Student Is Set").show()
            return
        }
        val student = studentDocumentSnapshot!!.toObject(Student::class.java)!!
        binding.apply {
            editFirstname.setText(student.firstname)
            editLastname.setText(student.lastname)
            editClass.setText(student.std_class)
            editAge.setText(student.age)
            editNotes.setText(student.notes)

            when (student.gender) {
                "Male" -> {
                    maleRadioBtn.isChecked = true
                }
                "Female" -> {
                    femaleRadioBtn.isChecked = true

                }
                "Other" -> {
                    otherRadioBtn.isChecked = true

                }
            }

        }


    }

    private fun setUpToolbar() {
        binding.toolbar.root.title = "Update Student"
    }


    private fun setOnClickListener() {
        create_button.setOnClickListener { view ->
            addStudentToDatabase(view)

        }
    }


    fun addStudentToDatabase(v: View?) {

        binding.apply {

            if (editFirstname!!.text!!.isBlank() || editLastname!!.text!!.isBlank() || editAge!!.text!!.isBlank() || editNotes!!.text!!.isBlank() || editClass!!.text!!.isBlank()) {
                Toasty.error(applicationContext, "Provide all fields", Toast.LENGTH_LONG).show()
            } else {
                val student: Student
                student = Student()
                student.firstname = editFirstname!!.text.toString()
                student.lastname = editLastname!!.text.toString()
                student.age = editAge!!.text.toString()// set age

                student.notes = editNotes!!.text.toString()
                student.std_class = editClass!!.text.toString()

                if (radioGroup.checkedRadioButtonId == R.id.maleRadioBtn) {
                    student.gender = "Male"
                } else if (radioGroup.checkedRadioButtonId == R.id.femaleRadioBtn) {
                    student.gender = "Female"
                } else {
                    student.gender = "Other"
                }

                postStudent(student)
            }

        }


    }

    fun postStudent(student: Student) {
        Log.d(TAG, "postStudent: student:$student")


        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)

        val programId = sharedPreferences.getString(Constants.KEY_PROGRAM_ID, null)
        val groupId = sharedPreferences.getString(Constants.KEY_GROUP_ID, null)
        val campId = sharedPreferences.getString(Constants.KEY_CAMP_ID, null)
        val campPos = sharedPreferences.getInt(Constants.CAMP_POS, -1)

        if (campPos == -1) {
            Toasty.error(this, "Please First create A camp before coming to this page", Toasty.LENGTH_LONG).show()
            onBackPressed()
        }


        showProgress(true)
        studentDocumentSnapshot!!.reference.set(student).addOnSuccessListener {
            studentDocumentSnapshot!!.reference.get().addOnSuccessListener {
                studentDocumentSnapshot=it
                showProgress(false)
                Toasty.success(this, "Success updating student").show()
                onBackPressed()

            }


            }.addOnFailureListener {
            showProgress(false)
            Log.e(TAG, "postStudent: Error", it)
        }


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

        dialog = setProgressDialog(this, "Loading..")
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
