package com.example.gam_project.repository

import androidx.annotation.WorkerThread
import com.example.gam_project.others.Constants.calendar_date
import com.example.gam_project.others.Constants.today_date
import com.example.gam_project.entity.TrackingEntity
import com.example.gam_project.dao.TrackingDao
import javax.inject.Inject

// 1
class TrackingRepository @Inject constructor(private val trackingDao: TrackingDao) {

    // 2
    val allTrackingEntities = trackingDao.getAllTrackingEntities(today_date)
    val lastTrackingEntity = trackingDao.getLastTrackingEntity()
    val totalDistanceTravelled = trackingDao.getTotalDistanceTravelled()
    val todayTotalDistanceTravelled = trackingDao.getTodayTotalDistanceTravelled(today_date)
    val calendarTotalDistanceTravelled =
        trackingDao.getCalendarTotalDistanceTravelled(calendar_date)

    // 3
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getLastTrackingEntityRecord() = trackingDao.getLastTrackingEntityRecord()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(trackingEntity: TrackingEntity) {
        trackingDao.insert(trackingEntity)
    }

//  @Suppress("RedundantSuspendModifier")
//  @WorkerThread
//  suspend fun deleteAll() {
//    trackingDao.deleteAll()
//  }

//  @Suppress("RedundantSuspendModifier")
//  @WorkerThread
//  suspend fun getAllTrackingEntitiesRecord() = trackingDao.getAllTrackingEntitiesRecord()


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getTodayAllTrackingEntities(today_date: String) =
        trackingDao.getTodayAllTrackingEntities(today_date)

    //캘린더
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getCalendarAllTrackingEntities(calendar_date: String) =
        trackingDao.getCalendarAllTrackingEntities(calendar_date)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteDailyRoute(calendar_date: String) {
        trackingDao.deleteDailyRoute(calendar_date)
    }

    @WorkerThread
    suspend fun deleteRoutes(id: String) {
        trackingDao.deleteRoutes(id)
    }

    @WorkerThread
    suspend fun getLastRoute(id: String) = trackingDao.getLastRoute(id)

    @WorkerThread
    suspend fun getRoutes(id: String) = trackingDao.getRoutes(id)

    @WorkerThread
    fun getRoutesLiveData(id: String) = trackingDao.getRoutesLiveData(id)

    @WorkerThread
    fun getIDTotalDistanceTravelled(id: String) = trackingDao.getIDTotalDistanceTravelled(id)

    @WorkerThread
    fun getStartTime(id: String) = trackingDao.getStartTime(id)

    @WorkerThread
    fun getEndTime(id: String) = trackingDao.getEndTime(id)
}
