package com.example.gam_project.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_record")
data class CalendarEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo val routeId: String,
    @ColumnInfo val startTime: String,
    @ColumnInfo val endTime: String,
    @ColumnInfo val year: Int,
    @ColumnInfo val month: Int,
    @ColumnInfo val day: Int,
    @ColumnInfo val title: String,
    @ColumnInfo val contents: String,
    @ColumnInfo val rating: Float
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(routeId)
        parcel.writeString(startTime)
        parcel.writeString(endTime)
        parcel.writeInt(year)
        parcel.writeInt(month)
        parcel.writeInt(day)
        parcel.writeString(title)
        parcel.writeString(contents)
        parcel.writeFloat(rating)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CalendarEntity> {
        override fun createFromParcel(parcel: Parcel): CalendarEntity {
            return CalendarEntity(parcel)
        }

        override fun newArray(size: Int): Array<CalendarEntity?> {
            return arrayOfNulls(size)
        }
    }
}
