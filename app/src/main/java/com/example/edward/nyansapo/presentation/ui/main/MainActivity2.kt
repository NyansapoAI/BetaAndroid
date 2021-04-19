package com.example.edward.nyansapo.presentation.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ActivityMain2Binding
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.presentation.ui.activities.ActivitiesFragment
import com.example.edward.nyansapo.presentation.ui.assessment.AssessmentFragment
import com.example.edward.nyansapo.presentation.ui.attendance.StudentAttendance
import com.example.edward.nyansapo.presentation.ui.grouping.GroupingFragment2
import com.example.edward.nyansapo.presentation.ui.grouping.REQUEST_CODE
import com.example.edward.nyansapo.presentation.ui.home.HomePageFragment
import com.example.edward.nyansapo.presentation.ui.patterns.PatternsFragment
import com.example.edward.nyansapo.presentation.utils.Constants
import com.example.edward.nyansapo.presentation.utils.FirebaseUtils
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.nightonke.blurlockview.BlurLockView
import com.nightonke.blurlockview.Directions.HideType
import com.nightonke.blurlockview.Directions.ShowType
import com.nightonke.blurlockview.Eases.EaseType
import com.nightonke.blurlockview.Password
import com.opencsv.CSVWriter
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class MainActivity2 : AppCompatActivity() {

    private var settingPassword = false
    private var counter = 0
    private val DEFAULT_PASSWORD = "1234"
    private val KEY_PASSWORD = "password"
    private val KEY_PASSWORD_ENABLED = "passwordEnabled"
    private val HIDE_DURATION_BLUR_VIEW = 1000
    lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val TAG = "MainActivity2"

        @JvmField
        var activityContext: MainActivity2? = null

    }

    lateinit var binding: ActivityMain2Binding

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        initProgressBar()

        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE)



        setUpToolbar()

        activityContext = this
        setUpNavigationDrawer()

        binding.bottomNavigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        //set default selected item
        binding.bottomNavigation.selectedItemId = R.id.action_home


    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        setUpBlurLockView()

    }

    private fun setUpBlurLockView() {
        Log.d(TAG, "setUpBlurLockView: ")
        binding.blurLockView.apply {
            setBlurredView(binding.container)
            downsampleFactor = 1
            blurRadius = 1
            overlayColor = R.color.black
        }







        if (isPasswordEnabled()) {
            Log.d(TAG, "setUpBlurLockView: password enabled")
            passwordEnabled()
        } else {
            Log.d(TAG, "setUpBlurLockView: password disabled")
        }


    }

    val leftButtonClickListener = object : BlurLockView.OnLeftButtonClickListener {
        override fun onClick() {
            Log.d(TAG, "onClick: left button clicked")
        }
    }

    val onPasswordInputListener = object : BlurLockView.OnPasswordInputListener {
        override fun correct(inputPassword: String?) {
            Log.d(TAG, "correct: inputPassword:$inputPassword")



            if (settingPassword) {


                passwordSetSuccessFully()

            } else {
                binding.blurLockView.hide(HIDE_DURATION_BLUR_VIEW, HideType.FADE_OUT, EaseType.Linear)
            }
        }


        override fun incorrect(inputPassword: String?) {
            Log.d(TAG, "incorrect: inputPassword:$inputPassword : :")

            //logic for when person forgets password
            if (inputPassword != null) {

                if (inputPassword.equals(DEFAULT_PASSWORD)) {
                    binding.blurLockView.hide(HIDE_DURATION_BLUR_VIEW, HideType.FADE_OUT, EaseType.Linear)
                    return
                }

            }


            if (settingPassword) {
                if (counter == 0) {
                    Log.d(TAG, "incorrect: first attempt")
                    sharedPreferences.edit().putString(KEY_PASSWORD, inputPassword).apply()
                    confirmPassword()

                    return
                }


                passwordsDoNotMatch()


            } else {
                showToast("incorrect password")
            }


        }

        override fun input(inputPassword: String?) {
            Log.d(TAG, "input: inputPassword:$inputPassword")
        }
    }

    private fun passwordsDoNotMatch() {
        showToast("Passwords do not match")
        resetPasswordScreen()

    }

    private fun resetPasswordScreen() {

        binding.blurLockView.apply {
            visibility = View.VISIBLE
            hide(HIDE_DURATION_BLUR_VIEW, HideType.FADE_OUT, EaseType.Linear)

        }
    }


    private fun passwordSetSuccessFully() {
        Log.d(TAG, "passwordSetSuccessFully: password set successfully")
        showToast("password set successfully")
        sharedPreferences.edit().putBoolean(KEY_PASSWORD_ENABLED, true).apply()
        binding.blurLockView.hide(HIDE_DURATION_BLUR_VIEW, HideType.FADE_OUT, EaseType.Linear)

        settingPassword = false

    }

    private fun passwordEnabled() {
        Log.d(TAG, "passwordEnabled: ")
        binding.blurLockView.visibility = View.VISIBLE
        val password = sharedPreferences.getString("password", DEFAULT_PASSWORD)
        binding.blurLockView.setCorrectPassword(password)
        binding.blurLockView.apply {

            setTitle("Enter Password");
            setLeftButton("Cancel");
            setRightButton("Backspace");
            //setTypeface(getTypeface());
            setType(Password.NUMBER, true);
            setOnLeftButtonClickListener(leftButtonClickListener);
            setOnPasswordInputListener(onPasswordInputListener);
            show(HIDE_DURATION_BLUR_VIEW, ShowType.FADE_IN, EaseType.Linear)
        }


    }


    private fun isPasswordEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_PASSWORD_ENABLED, false)
    }


    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar.root)
    }


    private fun setUpNavigationDrawer() {
        val toolbar = binding.root.findViewById<Toolbar>(R.id.toolbar)
        val toggle = ActionBarDrawerToggle(
                this, binding.drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        binding.drawerLayout.setDrawerListener(toggle)
        toggle.syncState()

        val navigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(drawerListener)
    }

    val drawerListener = object : NavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {

            when (item.itemId) {
                R.id.exportDataItem -> {
                    exportData2()
                }
                R.id.settingsItem -> {

                }
                R.id.synceItem -> {
                    syncData()

                }
                R.id.tutorialItem -> {

                }
                R.id.logoutItem -> {
                    logoutClicked()
                }
            }

            val drawer = binding.drawerLayout
            drawer.closeDrawer(GravityCompat.START)
            return true
        }
    }

    private fun syncData() {
        Log.d(TAG, "syncData: started")
        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE)

        val programId = sharedPreferences.getString(Constants.KEY_PROGRAM_ID, null)
        val groupId = sharedPreferences.getString(Constants.KEY_GROUP_ID, null)
        val campId = sharedPreferences.getString(Constants.KEY_CAMP_ID, null)
        val campPos = sharedPreferences.getInt(Constants.CAMP_POS, -1)

        Log.d(TAG, "onNavigationItemSelected: programId:$programId: :groupId:$groupId: :campId:$campId")

        if (campId != null) {
            startFetchingData(programId, groupId, campId)
        } else {
            Toasty.error(this, "Please First create a camp").show()
        }
    }

    private fun startFetchingData(programId: String?, groupId: String?, campId: String) {
        Log.d(TAG, "startFetchingData: started")


        FirebaseUtils.getFirstCamp(programId!!, groupId!!) {

            val DEFAULT_CAMP = it.documents.get(0).id

            FirebaseUtils.getCollectionStudentFromCamp_ReturnSnapshot(programId!!, groupId!!, DEFAULT_CAMP) {
                Log.d(TAG, "startFetchingData: number of students in original camp:${it.size()}")

                for (snapshot in it) {
                    val student = snapshot.toObject(Student::class.java)

                    FirebaseUtils.getCollectionStudentFromCamp_ReturnCollection(programId, groupId, campId).document(student.id!!).set(student).addOnSuccessListener {
                        Log.d(TAG, "startFetchingData: success adding student:$student")
                    }

                }

            }
        }

    }

    private fun logoutClicked() {
        AuthUI.getInstance().signOut(this).addOnSuccessListener {
            Log.d(TAG, "logoutClicked: sucess")
            finish()
        }
    }

    val onNavigationItemSelectedListener = object : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {

            val sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE)

            val programId = sharedPreferences.getString(Constants.KEY_PROGRAM_ID, null)
            val groupId = sharedPreferences.getString(Constants.KEY_GROUP_ID, null)
            val campId = sharedPreferences.getString(Constants.KEY_CAMP_ID, null)
            val campPos = sharedPreferences.getInt(Constants.CAMP_POS, -1)

            Log.d(TAG, "onNavigationItemSelected: programId:$programId: :groupId:$groupId: :campId:$campId")

            if (campId == null) {

                if (item.itemId == R.id.action_home) {

                    supportFragmentManager.beginTransaction().replace(R.id.container, HomePageFragment()).commit()
                    return true
                }

                Toasty.error(this@MainActivity2, "Please First create A camp", Toasty.LENGTH_LONG).show()
                return false
            } else {


                when (item.itemId) {
                    R.id.action_activities -> {
                        Log.d(TAG, "activities clicked: ")


                        supportFragmentManager.beginTransaction().replace(R.id.container, ActivitiesFragment()).commit()

                    }
                    R.id.action_grouping -> {
                        Log.d(TAG, "grouping clicked: ")

                        supportFragmentManager.beginTransaction().replace(R.id.container, GroupingFragment2()).commit()

                    }
                    R.id.action_home -> {
                        Log.d(TAG, "home clicked: ")

                        supportFragmentManager.beginTransaction().replace(R.id.container, HomePageFragment()).commit()

                    }
                    R.id.action_assess -> {
                        Log.d(TAG, "assessment clicked: ")

                        supportFragmentManager.beginTransaction().replace(R.id.container, AssessmentFragment()).commit()
                    }


                    R.id.action_patterns -> {
                        Log.d(TAG, "patterns clicked: ")

                        supportFragmentManager.beginTransaction().replace(R.id.container, PatternsFragment()).commit()

                    }
                }
            }
            return true
        }
    }


    fun exportData2() {
        Log.d(TAG, "exportData: ")
        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)

        val programId = sharedPreferences.getString(Constants.KEY_PROGRAM_ID, null)
        val groupId = sharedPreferences.getString(Constants.KEY_GROUP_ID, null)
        val campId = sharedPreferences.getString(Constants.KEY_CAMP_ID, null)
        val campPos = sharedPreferences.getInt(Constants.CAMP_POS, -1)

        if (campPos == -1) {
            Toasty.error(this, "Please First create A camp before coming to this page", Toasty.LENGTH_LONG).show()
            return
        }

        FirebaseUtils.getCollectionStudentFromCamp_ReturnSnapshot(programId, groupId, campId) {
            Log.d(TAG, "exportData: finished fetching students")
            val students = it.toObjects(Student::class.java)
            val data = StringBuilder()
            val header = "Day1,Day2,Day3,Day4,Day5,Firstname,Lastname,Age,Gender,Class,BaseLine,Endline".split(",").toTypedArray() // generate headers
            val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/Nyansapo_data")
            directory.mkdirs()
            val fileName = UUID.randomUUID().toString()
            val file = File(directory, "$fileName.csv")

            file.createNewFile()
            val writer: CSVWriter
            writer = CSVWriter(FileWriter(file.absolutePath));
            writer.writeNext(header);
            showProgress(true)
            lifecycleScope.launch {

                for (student in students!!) { // generate csv data
                    writeStudentData2(writer, student)
                }

                writer.close()
                Log.d(TAG, "exportData2: finished writing the csv file")

                showProgress(false)
                openCsvFile2(file)
            }


        }
    }


    private suspend fun writeStudentData2(writer: CSVWriter, student: Student) {
        val date1 = fetcheDate1(student)
        val date2 = fetcheDate2(student)
        val date3 = fetcheDate3(student)
        val date4 = fetcheDate4(student)
        val date5 = fetcheDate5(student)

        val row = "${date1},${date2},${date3},${date4},${date5},${student.firstname},${student.lastname},${student.age},${student.gender},${student.std_class},${student.baseLine},${student.learningLevel}".split(",").toTypedArray()
        // val row = "${student.firstname},${student.lastname},${student.age},${student.gender},${student.std_class},${student.baseLine},${student.learningLevel}".split(",").toTypedArray()
        Log.d(TAG, "writeStudentData2: row:${row}")
        writer.writeNext(row);


    }

    private suspend fun fetcheDate1(student: Student): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -4)
        val currentDate = cal.toDate

        val date = formatDate(currentDate)
        val it = checkPresentOrAbsent(student.id!!, date)
        Log.d(TAG, "fetchFirstData: status of student:$student:register:$it")
        return it

    }

    private suspend fun fetcheDate2(student: Student): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -3)
        val currentDate = cal.toDate

        val date = formatDate(currentDate)
        val it = checkPresentOrAbsent(student.id!!, date)
        Log.d(TAG, "fetchFirstData: status of student:$student:register:$it")
        return it

    }

    private suspend fun fetcheDate3(student: Student): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -2)
        val currentDate = cal.toDate

        val date = formatDate(currentDate)
        val it = checkPresentOrAbsent(student.id!!, date)
        Log.d(TAG, "fetchFirstData: status of student:$student:register:$it")
        return it

    }

    private suspend fun fetcheDate4(student: Student): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        val currentDate = cal.toDate

        val date = formatDate(currentDate)
        Log.d(TAG, "fetcheDate4: date:$date")
        val it = checkPresentOrAbsent(student.id!!, date)
        Log.d(TAG, "fetchFirstData: status of student:$student:register:$it")
        return it

    }

    private suspend fun fetcheDate5(student: Student): String {
        val currentDate = Calendar.getInstance().toDate
        val date = formatDate(currentDate)
        val it = checkPresentOrAbsent(student.id!!, date)
        Log.d(TAG, "fetchFirstData: status of student:$student:register:$it")
        return it

    }


    //check if student was present or absent
    private suspend fun checkPresentOrAbsent(id: String, date: String): String {
        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE)

        val programId = sharedPreferences.getString(Constants.KEY_PROGRAM_ID, null)
        val groupId = sharedPreferences.getString(Constants.KEY_GROUP_ID, null)
        val campId = sharedPreferences.getString(Constants.KEY_CAMP_ID, null)
        val campPos = sharedPreferences.getInt(Constants.CAMP_POS, -1)

        if (campPos == -1) {
            Toasty.error(this, "Please First create A camp before coming to this page", Toasty.LENGTH_LONG).show()
            supportFragmentManager.popBackStackImmediate()
        }


        val it = FirebaseUtils.getStudentFromAttendance_Task(programId, groupId, campId, id, date).await()
        val register: String
        if (it.exists()) {
            val studentAttendance = it.toObject(StudentAttendance::class.java)!!
            Log.d(TAG, "studentId: studentAttendance:$studentAttendance")
            if (studentAttendance.present) {
                register = "Present"
            } else {
                register = "Absent"
            }
        } else {
            register = "Present"
        }

        return register

    }

    val Calendar.toDate get() = this.time
    val Date.toCalendar
        get() = {
            val cal = Calendar.getInstance()
            cal.time = this
            cal
        }

    private fun formatDate(dateOriginal: Date): String {


        val date = SimpleDateFormat("dd_MM_yyyy").format(dateOriginal)
        Log.d(TAG, "getCurrentDateAndInitCurrentInfo: retrieving current date from database ${date}")

        //this symbols act weird with database
        var currentDate: String
        currentDate = date.replace("/", "_")
        currentDate = currentDate.replace("0", "")
        Log.d(TAG, "formatDate: ")
        return currentDate
    }


    private fun openCsvFile2(file: File) {
        val path = FileProvider.getUriForFile(this, "com.example.edward.nyansapo.fileprovider", file)
        val fileIntent = Intent(Intent.ACTION_SEND)
        fileIntent.type = "text/csv"
        fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Nyansapo Data")
        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        fileIntent.putExtra(Intent.EXTRA_STREAM, path)
        startActivity(Intent.createChooser(fileIntent, "Export Data"))

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item!!.itemId) {
            R.id.enablePasswordItem -> {
                enablePassword()
            }
            R.id.disablePasswordItem -> {
                disablePassword()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun disablePassword() {
        Log.d(TAG, "disablePassword: password disabled")
        sharedPreferences.edit().putBoolean(KEY_PASSWORD_ENABLED, false).apply()
        binding.blurLockView.hide(HIDE_DURATION_BLUR_VIEW, HideType.FADE_OUT, EaseType.Linear)

    }

    private fun enablePassword() {
        binding.blurLockView.visibility = View.VISIBLE

        Log.d(TAG, "enablePin: ")
        counter = 0
        settingPassword = true

        val password = sharedPreferences.getString("password", DEFAULT_PASSWORD)
        binding.blurLockView.setCorrectPassword("abcd")
        binding.blurLockView.apply {

            setTitle("Please Input Your Password");
            //  setLeftButton("Cancel");
            setRightButton("Backspace");
            //setTypeface(getTypeface());
            setType(Password.NUMBER, true);
            setOnLeftButtonClickListener(leftButtonClickListener);
            setOnPasswordInputListener(onPasswordInputListener);
            show(HIDE_DURATION_BLUR_VIEW, ShowType.FADE_IN, EaseType.Linear)
        }

    }

    private fun confirmPassword() {
        Log.d(TAG, "confirmPassword: ")
        counter++
        binding.blurLockView.visibility = View.VISIBLE


        val password = sharedPreferences.getString(KEY_PASSWORD, DEFAULT_PASSWORD)
        binding.blurLockView.setCorrectPassword(password)
        binding.blurLockView.apply {

            setTitle("Please Confirm Your Password");
            setLeftButton("Cancel");
            setRightButton("x");
            setType(Password.NUMBER, true);
            setOnLeftButtonClickListener(leftButtonClickListener);
            setOnPasswordInputListener(onPasswordInputListener);
            show(HIDE_DURATION_BLUR_VIEW, ShowType.FADE_IN, EaseType.Linear)
        }

    }

    private fun showToast(message: String) {
        Toasty.info(this@MainActivity2, message, Toast.LENGTH_SHORT)
                .show()
    }

    override fun onBackPressed() {

        val fragmentCount = supportFragmentManager.backStackEntryCount
        Log.d(TAG, "onBackPressed: fragmentCount:$fragmentCount")

        val drawer = binding.drawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//go to grouping fragment
        if (requestCode == REQUEST_CODE) {
            supportFragmentManager.beginTransaction().replace(R.id.container, GroupingFragment2()).commit()
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