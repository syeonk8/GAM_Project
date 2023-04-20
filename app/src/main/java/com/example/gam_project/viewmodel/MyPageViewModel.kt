package com.example.gam_project.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.gam_project.repository.CalendarRepository
import com.example.gam_project.repository.TrackingRepository

class MyPageViewModel @ViewModelInject constructor(
    private val calendarRepository: CalendarRepository,
    private val trackingRepository: TrackingRepository
) : ViewModel() {
    val totalDistanceTravelled: LiveData<Float?> = trackingRepository.totalDistanceTravelled
    val totalRecord: LiveData<Int?> = calendarRepository.countMemo
}