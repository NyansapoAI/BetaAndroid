package com.example.edward.nyansapo.assess_process.paragraph

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ActivityParagraphAssessmentBinding
import com.example.edward.nyansapo.util.GlobalData

class ParagraphAssessmentFragment : Fragment(R.layout.activity_paragraph_assessment) {

    private lateinit var binding: ActivityParagraphAssessmentBinding
    private val TAG = "ParagraphAssessmentFrag"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= ActivityParagraphAssessmentBinding.bind(view)
        initProgressBar()
        setDefaults()

    }

    private fun setDefaults() {
        binding.imageView4.setImageResource(GlobalData.avatar)
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