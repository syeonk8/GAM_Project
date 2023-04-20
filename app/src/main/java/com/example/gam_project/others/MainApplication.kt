package com.example.gam_project.others

import android.app.Application
import com.example.gam_project.db.GamDatabase
import com.example.gam_project.tracking.R
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {

//    private val gamDatabase by lazy { GamDatabase.getDatabase(this) }
//    val trackingRepository by lazy { TrackingRepository(gamDatabase.getTrackingDao()) }

    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, this.getString(R.string.kakao_app_key))
    }
}