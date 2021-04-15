package com.example.edward.nyansapo.presentation.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ActivityMain2Binding
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.presentation.ui.activities.ActivitiesFragment2
import com.example.edward.nyansapo.presentation.ui.assessment.AssessmentFragment
import com.example.edward.nyansapo.presentation.ui.grouping.GroupingFragment3
import com.example.edward.nyansapo.presentation.ui.home.HomePageFragment2
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
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import java.io.File


@AndroidEntryPoint
class MainActivity2 : AppCompatActivity() {

    private var settingPassword = false
    private var counter = 0
    private val DEFAULT_PASSWORD = "1234"
    private val KEY_PASSWORD = "password"
    private val KEY_PASSWORD_ENABLED = "passwordEnabled"
    private val HIDE_DURATION_BLUR_VIEW = 1000


    private val TAG = "MainActivity2"


companion object{
    @JvmField
    var activityContext: MainActivity2? = null
    lateinit var sharedPref: SharedPreferences
}




    lateinit var binding: ActivityMain2Binding

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPref = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE)



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
                    sharedPref.edit().putString(KEY_PASSWORD, inputPassword).apply()
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
        sharedPref.edit().putBoolean(KEY_PASSWORD_ENABLED, true).apply()
        binding.blurLockView.hide(HIDE_DURATION_BLUR_VIEW, HideType.FADE_OUT, EaseType.Linear)

        settingPassword = false

    }

    private fun passwordEnabled() {
        Log.d(TAG, "passwordEnabled: ")
        binding.blurLockView.visibility = View.VISIBLE
        val password = sharedPref.getString("password", DEFAULT_PASSWORD)
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
        return sharedPref.getBoolean(KEY_PASSWORD_ENABLED, false)
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
                    exportData()
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

                    supportFragmentManager.beginTransaction().replace(R.id.container, HomePageFragment2()).commit()
                    return true
                }

                Toasty.error(this@MainActivity2, "Please First create A camp", Toasty.LENGTH_LONG).show()
                return false
            } else {


                when (item.itemId) {
                    R.id.action_activities -> {
                        Log.d(TAG, "activities clicked: ")


                        supportFragmentManager.beginTransaction().replace(R.id.container, ActivitiesFragment2()).commit()

                    }
                    R.id.action_grouping -> {
                        Log.d(TAG, "grouping clicked: ")

                        supportFragmentManager.beginTransaction().replace(R.id.container, GroupingFragment3()).commit()

                    }
                    R.id.action_home -> {
                        Log.d(TAG, "home clicked: ")

                        supportFragmentManager.beginTransaction().replace(R.id.container, HomePageFragment2()).commit()

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


    fun exportData() {
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

            val students = it.toObjects(Student::class.java)
            val data = StringBuilder()
            data.append("Firstname,Lastname,Age,Gender,Class,Learning_Level") // generate headers
            for (student in students!!) { // generate csv data
                data.append("""
    
    ${student.firstname},${student.lastname},${student.age},${student.gender},${student.std_class},${student.learningLevel}
    """.trimIndent())
            }
            try {
                // save file before sending
                val out = openFileOutput("NyansapoData.csv", MODE_PRIVATE)
                out.write(data.toString().toByteArray())
                out.close()

                // export file
                val context = applicationContext
                val filelocation = File(filesDir, "NyansapoData.csv")
                val path = FileProvider.getUriForFile(context, "com.example.edward.nyansapo.fileprovider", filelocation)
                val fileIntent = Intent(Intent.ACTION_SEND)
                fileIntent.type = "text/csv"
                fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Nyansapo Data")
                fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                fileIntent.putExtra(Intent.EXTRA_STREAM, path)
                startActivity(Intent.createChooser(fileIntent, "Export Data"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
        sharedPref.edit().putBoolean(KEY_PASSWORD_ENABLED, false).apply()
        binding.blurLockView.hide(HIDE_DURATION_BLUR_VIEW, HideType.FADE_OUT, EaseType.Linear)

    }

    private fun enablePassword() {
        binding.blurLockView.visibility = View.VISIBLE

        Log.d(TAG, "enablePin: ")
        counter = 0
        settingPassword = true

        val password = sharedPref.getString("password", DEFAULT_PASSWORD)
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


        val password = sharedPref.getString(KEY_PASSWORD, DEFAULT_PASSWORD)
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

}



//program
var SharedPreferences.programId: String?
    get() = this.getString(Constants.KEY_PROGRAM_ID, null)
    set(value) = this.edit().putString(Constants.KEY_PROGRAM_ID, value).apply()

var SharedPreferences.programPos: Int
    get() = this.getInt(Constants.PROGRAM_POS, -1)
    set(value) = this.edit().putInt(Constants.PROGRAM_POS, value).apply()

//group
var SharedPreferences.groupId: String?
    get() = this.getString(Constants.KEY_GROUP_ID, null)
    set(value) = this.edit().putString(Constants.KEY_GROUP_ID, value).apply()

var SharedPreferences.groupPos: Int
    get() = this.getInt(Constants.GROUP_POS, -1)
    set(value) = this.edit().putInt(Constants.GROUP_POS, value).apply()

//camp
var SharedPreferences.campId: String?
    get() = this.getString(Constants.KEY_CAMP_ID, null)
    set(value) = this.edit().putString(Constants.KEY_CAMP_ID, value).apply()

var SharedPreferences.campPos: Int
    get() = this.getInt(Constants.CAMP_POS, -1)
    set(value) = this.edit().putInt(Constants.CAMP_POS, value).apply()




