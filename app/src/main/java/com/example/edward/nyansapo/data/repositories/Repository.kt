package com.example.edward.nyansapo.data.repositories

import com.example.edward.nyansapo.presentation.ui.activities.Activity

interface Repository {
    suspend fun getActivities(): List<Activity>

}