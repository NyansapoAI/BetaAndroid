package com.example.edward.nyansapo.data.repositories

import android.app.Activity
import androidx.lifecycle.LiveData
import com.example.edward.nyansapo.data.StaticData

class ProductionRepository:Repository {
    override suspend fun getActivities()= StaticData.getActivities()


}