package com.example.gam_project.entity

import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "tracking_record")
data class TrackingEntity(
    @PrimaryKey val timestamp: Long,
    @ColumnInfo val id: String,
    @ColumnInfo val currentTime: String,
    @ColumnInfo val latitude: Double,
    @ColumnInfo val longitude: Double
) {
    @ColumnInfo
    var distanceTravelled = 0f

    fun asLatLng() = LatLng(latitude, longitude)

    fun distanceTo(newTrackingEntity: TrackingEntity): Float {
        val locationA = Location("Previous Location")
        locationA.latitude = latitude
        locationA.longitude = longitude

        val locationB = Location("New Location")
        locationB.latitude = newTrackingEntity.latitude
        locationB.longitude = newTrackingEntity.longitude

        return locationA.distanceTo(locationB)
    }
}