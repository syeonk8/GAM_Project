package com.example.gam_project.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.gam_project.entity.CalendarEntity
import com.example.gam_project.repository.CalendarRepository
import java.util.*

class CalendarViewModel @ViewModelInject constructor(private val calendarRepository: CalendarRepository) :
    ViewModel() {
    val allDataDesc : LiveData<List<CalendarEntity>> = calendarRepository.allDataDesc.asLiveData()
    val allDataAsc : LiveData<List<CalendarEntity>> = calendarRepository.allDataAsc.asLiveData()
    val allDataRating : LiveData<List<CalendarEntity>> = calendarRepository.allDataRating.asLiveData()

    private val _selectedDate = MutableLiveData<Calendar?>(null)
    val entities = Transformations.switchMap(_selectedDate) {
        if (it == null) return@switchMap MutableLiveData<List<CalendarEntity>>(listOf())
        return@switchMap calendarRepository.getCalendarEntityListLiveData(
            it.get(Calendar.YEAR),
            it.get(Calendar.MONTH) + 1,
            it.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun setSelectedDate(date: Calendar) {
        _selectedDate.value = date
    }

    fun searchData(searchQuery: String) : LiveData<List<CalendarEntity>> {
        return calendarRepository.searchData(searchQuery).asLiveData()
    }

    //검색된 상태에서 정렬
    fun getSearchDataDesc(searchQuery: String) : LiveData<List<CalendarEntity>> {
        return calendarRepository.getSearchDataDesc(searchQuery).asLiveData()
    }

    fun getSearchDataAsc(searchQuery: String) : LiveData<List<CalendarEntity>> {
        return calendarRepository.getSearchDataAsc(searchQuery).asLiveData()
    }

    fun getSearchDataRating(searchQuery: String) : LiveData<List<CalendarEntity>> {
        return calendarRepository.getSearchDataRating(searchQuery).asLiveData()
    }


}