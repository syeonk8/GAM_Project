package com.example.gam_project

import android.app.Application
import com.example.gam_project.tracking.R
import com.kakao.sdk.common.KakaoSdk

// 1
class TrackingApplication: Application() {
  // 2
  private val trackingDatabase by lazy { TrackingDatabase.getDatabase(this) }
  val trackingRepository by lazy { TrackingRepository(trackingDatabase.getTrackingDao()) }

  override fun onCreate() {
    super.onCreate()

    KakaoSdk.init(this, this.getString(R.string.kakao_app_key))
  }
}
