package com.example.gam_project.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gam_project.dao.CalendarDao
import com.example.gam_project.entity.CalendarEntity
import com.example.gam_project.dao.TrackingDao
import com.example.gam_project.entity.TrackingEntity

@Database(entities = [TrackingEntity::class, CalendarEntity::class], version = 3)
abstract class GamDatabase : RoomDatabase() {
    abstract fun getTrackingDao(): TrackingDao
    abstract fun getCalendarDao(): CalendarDao

    companion object {
        @Volatile
        private var INSTANCE: GamDatabase? = null

        fun getDatabase(context: Context): GamDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE = Room.databaseBuilder(context.applicationContext, GamDatabase::class.java, "gam_database").build()
                INSTANCE!!
            }
        }
    }
}

