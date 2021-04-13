package com.example.edward.nyansapo.presentation.utils


import com.edward.nyansapo.R
import com.example.edward.nyansapo.data.local.db.AssessmentRecording

class GlobalData {
    companion object {
        @JvmField
        var avatar: Int = R.drawable.nyansapo_avatar_lion
    @JvmField
        var assessmentRecording: AssessmentRecording = AssessmentRecording()
    }
}