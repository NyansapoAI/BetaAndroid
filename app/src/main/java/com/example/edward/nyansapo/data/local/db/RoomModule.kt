package com.example.edward.nyansapo.data.local.db

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(
            app: Application,
    ) = Room.databaseBuilder(app, AssessmentDatabase::class.java, "assessment_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTaskDao(db: AssessmentDatabase) = db.assessmentDao()


}

