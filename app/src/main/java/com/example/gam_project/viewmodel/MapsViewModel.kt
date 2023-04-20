package com.example.gam_project.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gam_project.entity.TrackingEntity
import com.example.gam_project.repository.TrackingRepository
import kotlinx.coroutines.launch


class MapsViewModel @ViewModelInject constructor(private val trackingRepository: TrackingRepository) :
    ViewModel() {

//    val allTrackingEntities: LiveData<List<TrackingEntity>> = trackingRepository.allTrackingEntities
    val lastTrackingEntity: LiveData<TrackingEntity?> = trackingRepository.lastTrackingEntity
//    val todayTotalDistanceTravelled: LiveData<Float?> =
//        trackingRepository.todayTotalDistanceTravelled
//    val allTodayTrackingEntities: MutableLiveData<List<TrackingEntity>> = MutableLiveData(listOf())


    fun insert(trackingEntity: TrackingEntity) = viewModelScope.launch {
        trackingRepository.getLastRoute(trackingEntity.id)?.let {
            trackingEntity.distanceTravelled = trackingEntity.distanceTo(it)
        }
        trackingRepository.insert(trackingEntity)
    }

//    suspend fun deleteRoutes(id: String) {
//        trackingRepository.deleteRoutes(id)
//    }

//    fun getTodayAllTrackingEntities(today_date: String) = viewModelScope.launch {
//        allTodayTrackingEntities.value = trackingRepository.getTodayAllTrackingEntities(today_date)
//    }

    fun getIDTotalDistanceTravelled(id: String) = trackingRepository.getIDTotalDistanceTravelled(id)

    suspend fun getRoutes(id: String) = trackingRepository.getRoutes(id)
}