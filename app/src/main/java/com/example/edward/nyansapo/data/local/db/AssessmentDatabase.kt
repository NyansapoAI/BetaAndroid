package com.example.edward.nyansapo.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AssessmentRecording::class], version = 3,exportSchema = false)
abstract class AssessmentDatabase : RoomDatabase() {

    abstract fun assessmentDao(): AssessmentDao


    }
