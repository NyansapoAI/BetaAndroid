package com.example.edward.nyansapo.assess_process

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ActivityAssessProcessBinding

class AssessProcessActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAssessProcessBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssessProcessBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}