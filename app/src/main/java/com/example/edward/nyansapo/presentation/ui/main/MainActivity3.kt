package com.example.edward.nyansapo.presentation.ui.main

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.edward.nyansapo.R
import com.example.edward.nyansapo.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity3 : AppCompatActivity() {

    companion object{
        @JvmField
        var activityContext: MainActivity2? = null
        lateinit var sharedPref: SharedPreferences
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        MainActivity2.sharedPref = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE)

    }
}