package com.example.gam_project.others

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.gam_project.MainActivity
import com.example.gam_project.tracking.R

object TrackingNotification {

    const val CHANNEL_ID = "foreground_service_channel" // 임의의 채널 ID

    fun createNotification(
        context: Context
    ): Notification {
        // 알림 클릭시 MainActivity로 이동됨
        val notificationIntent = Intent(context, MainActivity::class.java)
//        notificationIntent.action = MAIN
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent
            .getActivity(context, 0, notificationIntent, FLAG_MUTABLE)

        // 알림
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("감")
            .setContentText("감이 실행중입니다.")
            .setSmallIcon(R.mipmap.ic_launcher_gam)
            .setOngoing(true) // true 일경우 알림 리스트에서 클릭하거나 좌우로 드래그해도 사라지지 않음
            .setAutoCancel(false)  // 클릭 시 알림이 삭제되지 않도록 설정
            .setContentIntent(pendingIntent)
            .build()


        // Oreo 부터는 Notification Channel을 만들어야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Tracking Channel", // 채널표시명
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }

        return notification
    }
}