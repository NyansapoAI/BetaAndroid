package com.example.edward.nyansapo


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ViewGroup.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.edward.nyansapo.presentation.utils.Constants
import com.example.edward.nyansapo.presentation.utils.FirebaseUtils
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*
import com.edward.nyansapo.R

class studentDetails : AppCompatActivity(), View.OnClickListener {

    private val TAG = "studentDetails"

    lateinit var studentId: String

    // declare ui elements
    var graphView: GraphView? = null
    var student_name: TextView? = null
    var initial_level: TextView? = null
    var current_level: TextView? = null
    var assessments_taken: TextView? = null

    // buttons
    var settings_button: Button? = null
    var assessment_button: Button? = null
    var home_button: Button? = null

    // database
    var DataBaseHandler: dataBaseHandler? = null
    lateinit var assessmentList: ArrayList<Assessment>
    var student_id: String? = null

    //    lateinit var student: Student
    var students: ArrayList<*>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_details)
        initProgressBar()
        /*     studentId=intent.getStringExtra(STUDENT_ID)
             student = studentDocumentSnapshot!!.toObject(Student::class.java)!!
             //Toast.makeText(this,instructor_id, Toast.LENGTH_LONG ).show();
     */

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@studentDetails, student_assessments::class.java)
            //     intent.putExtra("student_activity", student)
            startActivity(intent)
        }
        DataBaseHandler = dataBaseHandler(this)
        assessmentList = ArrayList()
        //assessmentList = DataBaseHandler.getAllAssessment();


        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE)

        val programId = sharedPreferences.getString(Constants.KEY_PROGRAM_ID, null)
        val groupId = sharedPreferences.getString(Constants.KEY_GROUP_ID, null)
        val campId = sharedPreferences.getString(Constants.KEY_CAMP_ID, null)
        val campPos = sharedPreferences.getInt(Constants.CAMP_POS, -1)

        if (campPos == -1) {
            Toasty.error(this, "Please First create A camp before coming to this page", Toasty.LENGTH_LONG).show()
            supportFragmentManager.popBackStackImmediate()
        }

        showProgress(true)

        val malianyeri = "F6HeFFcB1PZ6TrcTUr3I"

        val malia = "bxC9TMv4vh7g0jvR0QZr"
        val maliaodhiambo = "TBhZEcimJkkOG4s5mzLD"
        val justeli = "EUj4JtZIxCnd2ef8WGdJ"
        FirebaseUtils.getAssessmentsFromStudent(programId, groupId, campId, justeli)
        {
            showProgress(false)

            assessmentList = it.toObjects(Assessment::class.java) as ArrayList<Assessment>


            setUpGraph()


        }
    }

    private fun setUpGraph() {
        Log.d(TAG, "setUpGraph: ")
        assessmentList.forEach {
            Log.d(TAG, "setUpGraph: assessment:$it")
        }

        student_name = findViewById(R.id.student_name)
        initial_level = findViewById(R.id.initial_level)
        current_level = findViewById(R.id.current_level)
        assessments_taken = findViewById(R.id.assessments_taken)
        assessments_taken!!.setText("0") // updated
        // student_name!!.setText(student.firstname + ' ' + student.lastname)

        // code for buttons
        //settings_button = findViewById(R.id.settings_button);
        assessment_button = findViewById(R.id.new_assessment)
        home_button = findViewById(R.id.home_button)


        // set onclick listeners
        assessment_button!!.setOnClickListener(this)
        home_button!!.setOnClickListener(this)
        if (assessmentList!!.size > 0) {

            Log.d(TAG, "setUpGraph: assessment size ${assessmentList.size}")
            //Toast.makeText(this, assessmentList.get(assessmentList.size()-1).getLEARNING_LEVEL(),Toast.LENGTH_LONG).show();
            graphView = findViewById<View>(R.id.graphview) as GraphView
            val series = LineGraphSeries<DataPoint>()
            val num = assessmentList!!.size
            assessments_taken!!.setText(Integer.toString(num))
            var i = 0
            while (i < num && i < 5) {

                val x = (i + 1).toDouble()
                val y = getLevelIndex(assessmentList!!.get(i).learningLevel).toDouble()
                Log.d(TAG, "setUpGraph: $x : $y :learning level:${assessmentList!!.get(i).learningLevel} ")
                series.appendData(DataPoint((i + 1).toDouble(), getLevelIndex(assessmentList!!.get(i).learningLevel).toDouble()), true, 5)
                i++
            }

            series.setAnimated(true)
            graphView!!.addSeries(series)
            graphView!!.title = "Literacy Level Vs. Time of Current Assessments"
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
            current_level!!.setText(assessmentList.get(num - 1).learningLevel)
            initial_level!!.setText(assessmentList.get(0).learningLevel)
        } else {
            graphView = findViewById<View>(R.id.graphview) as GraphView
            /*
                LineGraphSeries<DataPoint>series = new LineGraphSeries<DataPoint>();
                series.appendData(new DataPoint(1,1), true, 10); // add datapoints
                series.appendData(new DataPoint(2,3), true, 10);
                series.appendData(new DataPoint(3,3), true, 10);
                series.setAnimated(true); // animate datapoints */
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


            // set ui elements with student_activity data
            current_level!!.setText("UKN")
            initial_level!!.setText("Unkown")
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

    fun gohome(v: View?) { // function to handle home button
        val myIntent = Intent(baseContext, student_assessments::class.java)
        startActivity(myIntent)
    }

    fun goSettings(v: View?) { // function to handle settings button
        val myIntent = Intent(baseContext, settings::class.java)
        startActivity(myIntent)
    }

    fun goAssessment(v: View?) { // function to handle new assessment button
        val myIntent = Intent(baseContext, SelectAssessment::class.java)
           startActivity(myIntent)
    }

    override fun onClick(v: View) { // assign function for the onclick listener buttons
        when (v.id) {
            R.id.home_button -> gohome(v)
            R.id.new_assessment -> goAssessment(v)
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