package com.example.gam_project

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gam_project.tracking.R
import com.example.gam_project.tracking.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true &&
                permissions[android.Manifest.permission.ACCESS_MEDIA_LOCATION] == true) {
                // 권한이 허용되었다면 파일 및 미디어에 접근합니다.
            } else {
                // 권한이 거부되었다면 알림을 표시합니다.
                Toast.makeText(this, "파일 및 미디어 접근 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(android.Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 권한이 허용되어 있다면 파일 및 미디어에 접근합니다.
        } else {
            // 권한이 허용되어 있지 않다면 권한을 요청합니다.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissions(arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.ACCESS_MEDIA_LOCATION
                ), 100)
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.ACCESS_MEDIA_LOCATION
                    )
                )
            }
        }

        val actionBar : ActionBar? = supportActionBar
        actionBar?.hide()

        val navView: BottomNavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mapsFragment, R.id.calendarFragment, R.id.myRecordFragment, R.id.myPageFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    fun hideBottomNavigation(state: Boolean) {
        if (state)
            binding.navView.visibility = View.GONE
        else
            binding.navView.visibility = View.VISIBLE
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되었다면 파일 및 미디어에 접근합니다.
            } else {
                // 권한이 거부되었다면 알림을 표시합니다.
                Toast.makeText(this, "파일 및 미디어 접근 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

//    //menu - logout
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if(item.itemId == R.id.log_out) {
//            FirebaseAuth.getInstance().signOut()
//            val intent = Intent(this@MainActivity,LoginActivity::class.java)
//            startActivity(intent)
//            finish()
//            return true
//        }
//        return true
//    }
}