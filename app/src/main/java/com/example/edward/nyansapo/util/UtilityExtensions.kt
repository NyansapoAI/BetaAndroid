package com.example.edward.nyansapo.util


import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import com.example.edward.nyansapo.Assessment
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.numeracy.AssessmentNumeracy
import com.example.edward.nyansapo.presentation.ui.activities.Activity
import com.example.edward.nyansapo.presentation.ui.attendance.StudentAttendance
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.*

inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })
}


val <T> T.exhaustive: T
    get() = this

val String.cleanTranscriptionTxt
    get() = this.toLowerCase().replace(".", "")!!.replace(",", "")

val String.sentenceToList: List<String>
    get() = this.split(" ").map {
        it.trim()
    }.filter {
        it.isNotBlank()
    }

val Date.formatDate get() = SimpleDateFormat("dd/MM/yyyy").format(this)
val String.cleanString
    get() =
        this.replace("/", "_")


val DocumentSnapshot.student get() = this.toObject(Student::class.java)!!
val DocumentSnapshot.activity get() = this.toObject(Activity::class.java)!!
val DocumentSnapshot.assessment get() = this.toObject(Assessment::class.java)!!
val DocumentSnapshot.assessmentNumeracy get() = this.toObject(AssessmentNumeracy::class.java)!!
val DocumentSnapshot.studentAttendance get() = this.toObject(StudentAttendance::class.java)!!






/////////////////////PROGRESS_BAR////////////////////////////
lateinit var dialog: AlertDialog

 fun Context.showProgress(show: Boolean) {
     if (!::dialog.isInitialized){
        initProgressBar(this)
     }

    if (show) {
        dialog.show()

    } else {
        dialog.dismiss()

    }

}

 fun initProgressBar(context: Context) {

    dialog = setProgressDialog(context, "Loading..")
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
