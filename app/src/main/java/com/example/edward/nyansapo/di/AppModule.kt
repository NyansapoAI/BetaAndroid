package com.example.edward.nyansapo.di

import com.example.edward.nyansapo.presentation.ui.grouping.GroupingRepository
import com.example.edward.nyansapo.presentation.ui.grouping.Repository_G
import com.example.edward.nyansapo.data.repositories.ProductionRepository
import com.example.edward.nyansapo.data.repositories.Repository
import com.example.edward.nyansapo.presentation.ui.home.HomeRepository
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
    @Provides
    @Singleton
    fun provideRepository_G():Repository_G{

        return GroupingRepository()
    }
    @Provides
    @Singleton
    fun provideRepository_H():HomeRepository{
        return HomeRepository()
    }
}