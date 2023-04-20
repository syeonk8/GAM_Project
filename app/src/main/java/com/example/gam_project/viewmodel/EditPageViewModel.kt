package com.example.gam_project.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.gam_project.entity.CalendarEntity
import com.example.gam_project.repository.CalendarRepository
import com.example.gam_project.repository.TrackingRepository


class EditPageViewModel @ViewModelInject constructor(
    private val calendarRepository: CalendarRepository,
    private val trackingRepository: TrackingRepository
) : ViewModel() {
    suspend fun getRoutes(id: String) = trackingRepository.getRoutes(id)

    suspend fun update(entity: CalendarEntity) {
        calendarRepository.updateMemo(entity)
    }
}