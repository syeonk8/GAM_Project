package com.example.gam_project.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gam_project.entity.TrackingEntity
import com.example.gam_project.repository.TrackingRepository
import kotlinx.coroutines.launch

class DailyViewModel @ViewModelInject constructor(
    private val trackingRepository: TrackingRepository
) : ViewModel() {
    val calendarTotalDistanceTravelled: LiveData<Float?> = trackingRepository.calendarTotalDistanceTravelled
    val calendarAllTrackingEntities: MutableLiveData<List<TrackingEntity>> = MutableLiveData(listOf())

    fun getCalendarAllTrackingEntities(calendar_date: String) = viewModelScope.launch {
        calendarAllTrackingEntities.value = trackingRepository.getCalendarAllTrackingEntities(calendar_date)
    }
}