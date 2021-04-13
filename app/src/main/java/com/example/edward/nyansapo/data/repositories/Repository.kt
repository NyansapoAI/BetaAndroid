package com.example.edward.nyansapo.data.repositories

import androidx.lifecycle.LiveData
import com.example.edward.nyansapo.data.models.ui.activities.Activity
import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun getActivities(): List<Activity>

}