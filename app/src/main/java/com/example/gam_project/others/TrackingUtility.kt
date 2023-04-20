package com.example.gam_project.others

import android.Manifest
import android.content.Context
import android.os.Build
import com.vmadalin.easypermissions.EasyPermissions

//function만 넣을거기 때문에 object로 생성
object TrackingUtility {

    fun hasLocationPermissions(context: Context) =
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){ //안드로이드 10 미만일 경우
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        }else{
            EasyPermissions.hasPermissions(
                //안드로이드 10 이상일 경우
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION, //백그라운드 옵션 추가
            )
        }

}