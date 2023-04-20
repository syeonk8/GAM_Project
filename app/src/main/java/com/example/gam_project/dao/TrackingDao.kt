package com.example.gam_project.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gam_project.entity.TrackingEntity

@Dao
interface TrackingDao {
    // 1
//  @Query("SELECT * FROM tracking_record")
//  fun getAllTrackingEntities(): LiveData<List<TrackingEntity>>

    //2
    @Query("SELECT SUM(distanceTravelled) FROM tracking_record")
    fun getTotalDistanceTravelled(): LiveData<Float?>

    // 3
    @Query("SELECT * FROM tracking_record ORDER BY timestamp DESC LIMIT 1")
    fun getLastTrackingEntity(): LiveData<TrackingEntity?>

    // 4
    @Query("SELECT * FROM tracking_record ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastTrackingEntityRecord(): TrackingEntity?

    // 5
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(trackingEntity: TrackingEntity)

    // 6
//  @Query("DELETE FROM tracking_record")
//  suspend fun deleteAll()

    // 7
//  @Query("SELECT * FROM tracking_record")
//  suspend fun getAllTrackingEntitiesRecord(): List<TrackingEntity>

    //하루 경로(polyline,총거리) 관련
    @Query("SELECT * FROM tracking_record WHERE currentTime = :today_date")
    fun getAllTrackingEntities(today_date: String): LiveData<List<TrackingEntity>>

    @Query("SELECT * FROM tracking_record WHERE currentTime = :today_date ORDER BY timestamp")
    suspend fun getTodayAllTrackingEntities(today_date: String): List<TrackingEntity>

    @Query("SELECT SUM(distanceTravelled) FROM tracking_record WHERE currentTime = :today_date")
    fun getTodayTotalDistanceTravelled(today_date: String): LiveData<Float?>

    //캘린더 관련
    @Query("SELECT * FROM tracking_record WHERE currentTime = :calendar_date ORDER BY timestamp")
    suspend fun getCalendarAllTrackingEntities(calendar_date: String): List<TrackingEntity>

    @Query("SELECT SUM(distanceTravelled) FROM tracking_record WHERE currentTime = :calendar_date")
    fun getCalendarTotalDistanceTravelled(calendar_date: String): LiveData<Float?>

    @Query("DELETE FROM tracking_record WHERE currentTime = :calendar_date")
    suspend fun deleteDailyRoute(calendar_date: String)

    @Query("DELETE FROM tracking_record WHERE id = :id")
    suspend fun deleteRoutes(id: String)

    @Query("SELECT * FROM tracking_record where id = :id ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastRoute(id: String): TrackingEntity?

    @Query("SELECT * FROM tracking_record where id = :id ORDER BY timestamp ASC")
    suspend fun getRoutes(id: String): List<TrackingEntity>

    @Query("SELECT * FROM tracking_record where id = :id ORDER BY timestamp ASC")
    fun getRoutesLiveData(id: String): LiveData<List<TrackingEntity>>

    @Query("SELECT SUM(distanceTravelled) FROM tracking_record WHERE id = :id")
    fun getIDTotalDistanceTravelled(id: String): LiveData<Float?>

    @Query("SELECT timestamp FROM tracking_record where id = :id ORDER BY timestamp DESC LIMIT 1")
    fun getEndTime(id: String): Long

    @Query("SELECT timestamp FROM tracking_record where id = :id ORDER BY timestamp ASC LIMIT 1")
    fun getStartTime(id: String): Long
}