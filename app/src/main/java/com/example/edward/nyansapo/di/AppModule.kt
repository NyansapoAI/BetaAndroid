package com.example.edward.nyansapo.di

import com.example.edward.nyansapo.data.repositories.ProductionRepository
import com.example.edward.nyansapo.data.repositories.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRepository():Repository{

        return ProductionRepository()
    }
}