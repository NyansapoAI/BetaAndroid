package com.example.edward.nyansapo.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.edward.nyansapo.data.repositories.ProductionRepository
import com.example.edward.nyansapo.data.repositories.Repository
import com.example.edward.nyansapo.numeracy.count_and_match.NumeracyRepository
import com.example.edward.nyansapo.presentation.ui.assessment.AssessmentRepo
import com.example.edward.nyansapo.presentation.ui.assessment.BeginAssessmentRepo
import com.example.edward.nyansapo.presentation.ui.grouping.GroupingRepository
import com.example.edward.nyansapo.presentation.ui.grouping.Repository_G
import com.example.edward.nyansapo.presentation.ui.home.HomeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRepository(): Repository {

        return ProductionRepository()
    }
    @Provides
    @Singleton
    fun providesAssRepo(): AssessmentRepo {

        return AssessmentRepo()
    }
   @Provides
    @Singleton
    fun providesBeginAssessmentRepo(): BeginAssessmentRepo {

        return BeginAssessmentRepo()
    }

    @Provides
    @Singleton
    fun provideNumeracyRepository(): NumeracyRepository {

        return NumeracyRepository()
    }

    @Provides
    @Singleton
    fun provideRequestManager(@ApplicationContext context: Context): RequestManager {

        return Glide.with(context)
    }

    @Provides
    @Singleton
    fun provideRepository_G(): Repository_G {

        return GroupingRepository()
    }

    @Provides
    @Singleton
    fun provideRepository_H(): HomeRepository {
        return HomeRepository()
    }
}