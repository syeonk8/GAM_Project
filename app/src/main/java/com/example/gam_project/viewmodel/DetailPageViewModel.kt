package com.example.gam_project.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.gam_project.entity.CalendarEntity
import com.example.gam_project.repository.CalendarRepository
import com.example.gam_project.repository.TrackingRepository


class DetailPageViewModel @ViewModelInject constructor(
    private val calendarRepository: CalendarRepository,
    private val trackingRepository: TrackingRepository
) : ViewModel() {
    fun getEntity(id: Int) = calendarRepository.getCalendarEntityLiveData(id)

    suspend fun getRoutes(id: String) = trackingRepository.getRoutes(id)

    suspend fun delete(entity: CalendarEntity) {
        trackingRepository.deleteRoutes(entity.routeId);
        calendarRepository.deleteMemo(entity)
    }
}