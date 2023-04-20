package com.example.gam_project.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.gam_project.entity.CalendarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMemo(calendarEntity: CalendarEntity)

    @Delete
    suspend fun deleteMemo(calendarEntity: CalendarEntity)

    @Update
    suspend fun updateMemo(calendarEntity: CalendarEntity)

    //검색 안했을 때 정렬
    @Query("SELECT * from calendar_record ORDER BY id DESC")
    fun getAllDataDesc(): Flow<List<CalendarEntity>>

    @Query("SELECT * from calendar_record ORDER BY id ASC")
    fun getAllDataAsc(): Flow<List<CalendarEntity>>

    @Query("SELECT * from calendar_record ORDER BY rating DESC, id DESC")
    fun getAllDataRating(): Flow<List<CalendarEntity>>

    //검색된 상태에서 정렬
    @Query("SELECT * from calendar_record where (title LIKE :searchQuery) OR (contents LIKE :searchQuery) ORDER BY id DESC")
    fun getSearchDataDesc(searchQuery: String): Flow<List<CalendarEntity>>

    @Query("SELECT * from calendar_record where (title LIKE :searchQuery) OR (contents LIKE :searchQuery) ORDER BY id ASC")
    fun getSearchDataAsc(searchQuery: String): Flow<List<CalendarEntity>>

    @Query("SELECT * from calendar_record where (title LIKE :searchQuery) OR (contents LIKE :searchQuery) ORDER BY rating DESC, id DESC")
    fun getSearchDataRating(searchQuery: String): Flow<List<CalendarEntity>>

    //정렬된된 상태에서 검색
    //

    @Query("SELECT * from calendar_record WHERE year = :year AND month = :month AND day = :day")
    fun getCalendarData(year: Int, month: Int, day: Int): List<CalendarEntity>

    @Query("SELECT COUNT(id) from calendar_record")
    fun getCountMemo(): LiveData<Int?>

    @Query("SELECT * FROM calendar_record where (title LIKE :searchQuery) OR (contents LIKE :searchQuery)")
    fun searchData(searchQuery: String): Flow<List<CalendarEntity>>

    @Query("SELECT * from calendar_record WHERE year = :year AND month = :month AND day = :day")
    fun getCalendarEntityListLiveData(
        year: Int,
        month: Int,
        day: Int
    ): LiveData<List<CalendarEntity>>

    @Query("SELECT * from calendar_record WHERE id = :id")
    fun getCalendarEntityLiveData(
        id: Int
    ): LiveData<CalendarEntity?>

    @Query("SELECT * from calendar_record WHERE year = :year AND month = :month AND day = :day")
    fun getCalendarCheckList(
        year: Int,
        month: Int,
        day: Int
    ): CalendarEntity?
}