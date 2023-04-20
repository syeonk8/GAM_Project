package com.example.gam_project.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gam_project.entity.CalendarEntity
import com.example.gam_project.repository.CalendarRepository
import com.example.gam_project.repository.TrackingRepository
import kotlinx.coroutines.launch


class RecordPageViewModel @ViewModelInject constructor(
    private val trackingRepository: TrackingRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    fun insert(calendarEntity: CalendarEntity) = viewModelScope.launch {
        calendarRepository.insertMemo(calendarEntity)
    }

    fun getStartTime(id: String) = trackingRepository.getStartTime(id)
    fun getEndTime(id: String) = trackingRepository.getEndTime(id)

    suspend fun getRoutes(id: String) = trackingRepository.getRoutes(id)
}