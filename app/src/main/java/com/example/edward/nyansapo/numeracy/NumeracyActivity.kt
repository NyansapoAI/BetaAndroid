package com.example.edward.nyansapo.numeracy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.edward.nyansapo.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NumeracyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_numeracy)
    }
}