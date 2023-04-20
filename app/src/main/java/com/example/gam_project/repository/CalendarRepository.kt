package com.example.gam_project.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.gam_project.entity.CalendarEntity
import com.example.gam_project.dao.CalendarDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CalendarRepository @Inject constructor(private val calendarDao: CalendarDao) {
    val allDataDesc: Flow<List<CalendarEntity>> = calendarDao.getAllDataDesc()
    val allDataAsc: Flow<List<CalendarEntity>> = calendarDao.getAllDataAsc()
    val allDataRating: Flow<List<CalendarEntity>> = calendarDao.getAllDataRating()
    val countMemo = calendarDao.getCountMemo()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertMemo(calendarEntity: CalendarEntity) {
        calendarDao.insertMemo(calendarEntity)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteMemo(calendarEntity: CalendarEntity) {
        calendarDao.deleteMemo(calendarEntity)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateMemo(calendarEntity: CalendarEntity) {
        calendarDao.updateMemo(calendarEntity)
    }

    @WorkerThread
    fun searchData(searchQuery: String): Flow<List<CalendarEntity>> {
        return calendarDao.searchData(searchQuery)
    }

    @WorkerThread
    fun getSearchDataDesc(searchQuery: String): Flow<List<CalendarEntity>> {
        return calendarDao.getSearchDataDesc(searchQuery)
    }

    @WorkerThread
    fun getSearchDataAsc(searchQuery: String): Flow<List<CalendarEntity>> {
        return calendarDao.getSearchDataAsc(searchQuery)
    }

    @WorkerThread
    fun getSearchDataRating(searchQuery: String): Flow<List<CalendarEntity>> {
        return calendarDao.getSearchDataRating(searchQuery)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun getCalendarData(year: Int, month: Int, day: Int): List<CalendarEntity> {
        return calendarDao.getCalendarData(year, month, day)
    }

    @WorkerThread
    fun getCalendarEntityListLiveData(
        year: Int,
        month: Int,
        day: Int
    ): LiveData<List<CalendarEntity>> {
        return calendarDao.getCalendarEntityListLiveData(year, month, day)
    }

    @WorkerThread
    fun getCalendarEntityLiveData(id: Int): LiveData<CalendarEntity?> {
        return calendarDao.getCalendarEntityLiveData(id)
    }

}