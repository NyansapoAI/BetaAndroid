package com.example.edward.nyansapo.numeracy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import com.edward.nyansapo.R
import com.example.edward.nyansapo.Student
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_student_details.*

@AndroidEntryPoint
class NumeracyActivity : AppCompatActivity() {

    private val TAG = "NumeracyActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_numeracy)

        val student = intent.extras.getParcelable<Student>("student")
        Log.d(TAG, "onCreate: student:$student")
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        navController.setGraph(R.navigation.nav_graph_numeracy, intent.extras)
    }
}