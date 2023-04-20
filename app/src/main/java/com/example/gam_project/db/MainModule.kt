package com.example.gam_project.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object MainModule {

    @Singleton
    @Provides
    fun provideGamDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        GamDatabase::class.java,
        "gam_database"
    ).fallbackToDestructiveMigration()
        .allowMainThreadQueries()
        .build()

    @Singleton
    @Provides
    fun provideTrackingDao(db: GamDatabase) = db.getTrackingDao()

    @Singleton
    @Provides
    fun provideCalendarDao(db: GamDatabase) = db.getCalendarDao()
}