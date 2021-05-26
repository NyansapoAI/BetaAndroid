package com.example.edward.nyansapo.presentation.ui.preassessment

import android.util.Log
import com.example.edward.nyansapo.util.FirebaseUtils
import com.example.edward.nyansapo.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.*

class MainRepository {

    private val TAG = "MainRepository"

    fun getCurrentDate() = callbackFlow<Resource<Date>> {
        offer(Resource.loading("Fetching current date..."))
        FirebaseUtils.getCurrentDate { date ->
            Log.d(TAG, "setCurrentDate: date retrieved:${date}")
            if (date == null) {
                val currentDateServer = Calendar.getInstance().time
                offer(Resource.success(currentDateServer))

            } else {
                offer(Resource.success(date))

            }


        }

        awaitClose { }
    }
}