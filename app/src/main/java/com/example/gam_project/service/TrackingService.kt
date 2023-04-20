package com.example.gam_project.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.gam_project.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.gam_project.others.Constants.ACTION_STOP_SERVICE
import com.example.gam_project.others.TrackingNotification
//import com.example.gam_project.others.Constants.NOTIFICATION_CHANNEL_ID
//import com.example.gam_project.others.Constants.NOTIFICATION_CHANNEL_NAME
//import com.example.gam_project.others.Constants.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
//class TrackingService : LifecycleService(){
//
//    //트래킹 시작 여부 확인용
////    var isFirstRun = true
//
//    //백그라운드 서비스
//
//    @RequiresApi(Build.VERSION_CODES.N)
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        intent?.let {
//            when(it.action) {
//                ACTION_START_OR_RESUME_SERVICE -> {
//                    if(ISTRACKING){
//                        startForegroundService()
//                    }
////                    if(isFirstRun){
////                        startForegroundService()
////                        isFirstRun = false
////                    }
//                    else{
//                        Log.d(TAG,"서비스 시작중..")
////                        startForegroundService()
//                    }
//                }
////                ACTION_PAUSE_SERVICE -> {
////                    Log.d(TAG,"서비스 일시 멈춤")
////                }
//                ACTION_STOP_SERVICE -> {
//                    Log.d(TAG,"서비스 정지")
//                    stopService() //더이상 위/경도 안받아옴
//                }
//                else -> {}
//            }
//        }
//        return super.onStartCommand(intent, flags, startId)
//    }
//
//
//    //백그라운드서비스 종료 함수
//    @RequiresApi(Build.VERSION_CODES.N)
//    private fun stopService() {
//        stopForeground(STOP_FOREGROUND_REMOVE)
//        stopSelf()
//    }
//
//    //포그라운드 서비스
//    private fun startForegroundService() {
//
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
//                as NotificationManager
//
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            createNotificationChannel(notificationManager)
//        }
//
//        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//            .setSmallIcon(R.mipmap.ic_launcher_gam)  //작은 아이콘 설정
//            .setWhen(System.currentTimeMillis()) //시간 설정 setShowWhen(false)- 숨기기
//            .setContentTitle("감") // 알림 메시지 설정
//            .setContentText("감이 실행중입니다.")// 알림 내용 설정
//            .setAutoCancel(false)  // 클릭 시 알림이 삭제되지 않도록 설정
//            .setOngoing(true) //스와이프 못함
//            .setContentIntent(getMainActivityPendingIntent())
//
//        startForeground(NOTIFICATION_ID, notificationBuilder.build())
//    }
//
//    //상단 탭 클릭시 이동
//    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
//        this,
//        0,
//        Intent(this, MainActivity::class.java).also {
//            it.action = ACTION_SHOW_MAPS_FRAGMENT//mapsfragment로 이동
//        },
////        FLAG_UPDATE_CURRENT
//        PendingIntent.FLAG_MUTABLE
//    )
//
//
//
//    //상단 탭 설정하기 위해 channel 생성
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun createNotificationChannel(notificationManager: NotificationManager) {
//        val channel = NotificationChannel(
//            NOTIFICATION_CHANNEL_ID,
//            NOTIFICATION_CHANNEL_NAME,
//            NotificationManager.IMPORTANCE_LOW
//        )
//        notificationManager.createNotificationChannel(channel)
//    }
//
//}

class TrackingService : Service() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "Action Received = ${intent?.action}")
        when (intent?.action) {
            ACTION_START_OR_RESUME_SERVICE -> {
                Log.e(TAG, "Start Foreground 인텐트를 받음")
                startForegroundService()
            }
            ACTION_STOP_SERVICE -> {
                Log.e(TAG, "Stop Foreground 인텐트를 받음")
                stopForegroundService()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = TrackingNotification.createNotification(this)
        startForeground(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        // bound service가 아니므로 null
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy()")
    }

    companion object {
        const val TAG = "[TrackingService]"
        const val NOTIFICATION_ID = 20
    }

}