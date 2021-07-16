package com.example.edward.nyansapo.presentation.ui.activities

import com.google.firebase.firestore.DocumentSnapshot

data class ActivitySections(val header: String, var sectionActivities: MutableList<Activity> = mutableListOf())
