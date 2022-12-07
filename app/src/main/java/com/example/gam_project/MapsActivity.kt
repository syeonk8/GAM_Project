package com.example.gam_project

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.gam_project.*
import com.example.gam_project.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.gam_project.Constants.ACTION_STOP_SERVICE
import com.example.gam_project.tracking.R
import com.example.gam_project.tracking.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted

import java.util.*


private const val TAG_CALENDER = "calender_fragment"
private const val TAG_MAP = "map_fragment"
private const val TAG_MY_PAGE = "my_page_fragment"

/**
 * Main Screen
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {
  private lateinit var binding: ActivityMainBinding

  private fun setFragment(tag: String, fragment: Fragment) {
    val manager: FragmentManager = supportFragmentManager
    val fragTransaction = manager.beginTransaction()

    if (manager.findFragmentByTag(tag) == null){
      fragTransaction.add(R.id.mainFrameLayout, fragment, tag)
    }
    val calender = manager.findFragmentByTag(TAG_CALENDER)
    val map = manager.findFragmentByTag(TAG_MAP)
    val myPage = manager.findFragmentByTag(TAG_MY_PAGE)


    if (calender != null){
      fragTransaction.hide(calender)
    }


    if (map != null) {
      fragTransaction.hide(map)
    }

    if (myPage != null) {
      fragTransaction.hide(myPage)
    }


    if (tag == TAG_CALENDER) {
      if (calender!=null){
        fragTransaction.show(calender)
      }
    }


    else if (tag == TAG_MAP){
      if (map != null){
        fragTransaction.show(map)
      }
    }

    else if (tag == TAG_MY_PAGE){
      if (myPage != null){
        fragTransaction.show(myPage)
      }
    }

    fragTransaction.commitAllowingStateLoss()
  }

  private lateinit var mMap: GoogleMap //구글 맵
  private lateinit var fusedLocationProviderClient: FusedLocationProviderClient //현재위치
  private var polylineOptions = PolylineOptions()

  companion object {
    // SharedPreferences
    private const val KEY_SHARED_PREFERENCE = "com.rwRunTrackingApp.sharedPreferences"
    private const val KEY_IS_TRACKING = "com.rwRunTrackingApp.isTracking"

    // Permission
    private const val REQUEST_CODE_FINE_LOCATION = 1
    private const val REQUEST_CODE_ACTIVITY_RECOGNITION = 2
  }

  // 뷰모델
  private val mapsActivityViewModel: MapsActivityViewModel by viewModels {
    MapsActivityViewModelFactory(getTrackingRepository())
  }

  //권한
  val permissons = arrayOf(ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
  val PERM_FLAG = 99


  private var isTracking: Boolean
    get() = this.getSharedPreferences(KEY_SHARED_PREFERENCE, MODE_PRIVATE).getBoolean(
      KEY_IS_TRACKING, false)
    set(value) = this.getSharedPreferences(KEY_SHARED_PREFERENCE, MODE_PRIVATE).edit().putBoolean(
      KEY_IS_TRACKING, value).apply()


  //현재위치 및 내부 db에 저장
  private val locationCallback = object: LocationCallback() {
    override fun onLocationResult(locationResult: LocationResult?) {
      super.onLocationResult(locationResult)
      locationResult ?: return
      locationResult.locations.forEach {
        val trackingEntity = TrackingEntity(Calendar.getInstance().timeInMillis, it.latitude, it.longitude)
        mapsActivityViewModel.insert(trackingEntity)
        Log.v("NEW_LOCATION", it.latitude.toString() + ", " + it.latitude.toString()) //log로 확인
      }
    }
  }


  override fun onCreate(savedInstanceState: Bundle?) {

    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setFragment(TAG_CALENDER,CalenderFragment()) //캘린더로 시작

    binding.navigationView.setOnItemSelectedListener { item ->
      when(item.itemId) {
        R.id.calenderFragment -> setFragment(TAG_CALENDER, CalenderFragment())
        R.id.map -> setFragment(TAG_MAP, MapFragment())
        R.id.myPageFragment -> setFragment(TAG_MY_PAGE, MyPageFragment())
      }
      true
    }


    //권한체크
    if(isPermitted()){
      startProcess()
    }else{
      ActivityCompat.requestPermissions(this,permissons,PERM_FLAG)
    }


    // 현재위치
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

    // 시작 버튼 클릭시
    binding.startButton.setOnClickListener {
      mMap.clear()
      isTracking = true

      //백그라운드/포그라운드 동작
      sendCommandToService(ACTION_START_OR_RESUME_SERVICE)

      updateButtonStatus()

      // 걸음수, 이동거리 초기화
      updateAllDisplayText(0, 0f)

      //트래킹 시작
      startTracking()
    }
    //종료버튼 클릭시
    binding.endButton.setOnClickListener {

      //서비스 종료
      sendCommandToService(ACTION_STOP_SERVICE)
      // Update layouts
      updateButtonStatus()
      //종료가 맞는지 확인
      endButtonClicked()
    }


    //viewmodel
    mapsActivityViewModel.allTrackingEntities.observe(this) { allTrackingEntities ->
      if (allTrackingEntities.isEmpty()) {
        updateAllDisplayText(0, 0f)
      }
    }

    mapsActivityViewModel.lastTrackingEntity.observe(this) { lastTrackingEntity ->
      lastTrackingEntity ?: return@observe
      addLocationToRoute(lastTrackingEntity)
    }

    mapsActivityViewModel.totalDistanceTravelled.observe(this) {
      it ?: return@observe
      val stepCount = mapsActivityViewModel.currentNumberOfStepCount.value ?: 0
      updateAllDisplayText(stepCount, it)
    }

    mapsActivityViewModel.currentNumberOfStepCount.observe(this) {
      val totalDistanceTravelled = mapsActivityViewModel.totalDistanceTravelled.value ?: 0f
      updateAllDisplayText(it, totalDistanceTravelled)
    }

    mapsActivityViewModel.allTrackingEntitiesRecord.observe(this) {
      addLocationListToRoute(it)
    }

    if (isTracking) {
      startTracking()
    }
  }

  // Repository
  private fun getTrackingApplicationInstance() = application as TrackingApplication
  private fun getTrackingRepository() = getTrackingApplicationInstance().trackingRepository

  // UI related codes
  private fun updateButtonStatus() {
    binding.startButton.isEnabled = !isTracking
    binding.endButton.isEnabled = isTracking
  }

  private fun updateAllDisplayText(stepCount: Int, totalDistanceTravelled: Float) {
    binding.numberOfStepTextView.text =  String.format("걸음 수: %d", stepCount)
    binding.totalDistanceTextView.text = String.format("총 거리: %.2fm", totalDistanceTravelled)

    val averagePace = if (stepCount != 0) totalDistanceTravelled / stepCount.toDouble() else 0.0
    binding.averagePaceTextView.text = String.format("평균 속도: %.2fm/ step", averagePace)
  }

  private fun endButtonClicked() {
    AlertDialog.Builder(this)
        .setTitle("오늘의 기록을 그만 하시겠습니까?")
        .setPositiveButton("확인") { _, _ ->
          isTracking = false
          updateButtonStatus()
          stopTracking()
        }.setNegativeButton("취소") { _, _ ->
        }
        .create()
        .show()
  }

  // 트래킹
  @AfterPermissionGranted(REQUEST_CODE_ACTIVITY_RECOGNITION)
  private fun startTracking() {
    val isActivityRecognitionPermissionFree = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    val isActivityRecognitionPermissionGranted = EasyPermissions.hasPermissions(this,
        ACTIVITY_RECOGNITION)
    Log.d("TAG", "Is ACTIVITY_RECOGNITION permission granted $isActivityRecognitionPermissionGranted")
    if (isActivityRecognitionPermissionFree || isActivityRecognitionPermissionGranted) {
      setupStepCounterListener()
      setupLocationChangeListener()
    } else {
      // Do not have permissions, request them now
      EasyPermissions.requestPermissions(
          host = this,
          rationale = "For showing your step counts and calculate the average pace.",
          requestCode = REQUEST_CODE_ACTIVITY_RECOGNITION,
          perms = *arrayOf(ACTIVITY_RECOGNITION)
      )
    }
  }

  private fun stopTracking() {
    polylineOptions = PolylineOptions()

    mapsActivityViewModel.deleteAllTrackingEntity()
    fusedLocationProviderClient.removeLocationUpdates(locationCallback)

    // 만보기 센서 설정
    val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    sensorManager.unregisterListener(this, stepCounterSensor)
  }

  //서비스 관련
  private fun sendCommandToService(action: String) =
    Intent(this, TrackingService::class.java).also {
      it.action = action
      this.startService(it)
    }

  fun isPermitted() : Boolean{
    for(perm in permissons){
      if(ContextCompat.checkSelfPermission(this,perm) != PERMISSION_GRANTED){
        return false
      }
    }
    return true
  }

  fun startProcess(){
    val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)
  }


  // Map related codes
  /**
   * Manipulates the map once available.
   * This callback is triggered when the map is ready to be used.
   * This is where we can add markers or lines, add listeners or move the camera. In this case,
   * we just add a marker near Sydney, Australia.
   * If Google Play services is not installed on the device, the user will be prompted to install
   * it inside the SupportMapFragment. This method will only be triggered once the user has
   * installed Google Play services and returned to the app.
   */
  @SuppressLint("MissingPermission")
  override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap

    showUserLocation()

    // 마커를 서울 시청으로 설정
    val latitude = 37.5666805
    val longitude = 126.9784147
    val seoulLatLong = LatLng(latitude, longitude)

    val zoomLevel = 9.5f
    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(seoulLatLong, zoomLevel))

    // polyline 그리기
    if (isTracking) {
      mapsActivityViewModel.getAllTrackingEntities()
    }
  }

  private fun addLocationListToRoute(trackingEntityList: List<TrackingEntity>) {
    if (!this::mMap.isInitialized) {
      return
    }
    mMap.clear()
    trackingEntityList.forEach { trackingEntity ->
      val newLatLngInstance = trackingEntity.asLatLng()
      polylineOptions.points.add(newLatLngInstance)
    }
    mMap.addPolyline(polylineOptions)
  }

  private fun addLocationToRoute(trackingEntity: TrackingEntity) {
    if (!this::mMap.isInitialized) {
      return
    }
    mMap.clear()
    val newLatLngInstance = trackingEntity.asLatLng()
    polylineOptions.points.add(newLatLngInstance)
    mMap.addPolyline(polylineOptions)
  }

  // 만보기 센서
  private fun setupStepCounterListener() {
    val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    stepCounterSensor ?: return
    sensorManager.registerListener(this@MapsActivity, stepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST)
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    Log.d("TAG", "onAccuracyChanged: Sensor: $sensor; accuracy: $accuracy")
  }

  override fun onSensorChanged(sensorEvent: SensorEvent?) {
    Log.d("TAG", "onSensorChanged")
    sensorEvent ?: return
    val firstSensorEvent = sensorEvent.values.firstOrNull() ?: return
    Log.d("TAG", "Steps count: $firstSensorEvent ")
    val isFirstStepCountRecord = mapsActivityViewModel.currentNumberOfStepCount.value == 0
    if (isFirstStepCountRecord) {
      mapsActivityViewModel.initialStepCount = firstSensorEvent.toInt()
      mapsActivityViewModel.currentNumberOfStepCount.value = 1
    } else {
      mapsActivityViewModel.currentNumberOfStepCount.value = firstSensorEvent.toInt() - mapsActivityViewModel.initialStepCount
    }
  }

  // Location
  @AfterPermissionGranted(REQUEST_CODE_FINE_LOCATION)
  private fun showUserLocation() {
    if (EasyPermissions.hasPermissions(this, ACCESS_FINE_LOCATION)) {
      if (ActivityCompat.checkSelfPermission(
          this,
          ACCESS_FINE_LOCATION
        ) != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
          this,
          Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PERMISSION_GRANTED
      ) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
      }
      if (ActivityCompat.checkSelfPermission(
          this,
          ACCESS_FINE_LOCATION
        ) != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
          this,
          Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PERMISSION_GRANTED
      ) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
      }
      mMap.isMyLocationEnabled = true
    } else {
      // Do not have permissions, request them now
      EasyPermissions.requestPermissions(
          host = this,
          rationale = "For showing your current location on the map.",
          requestCode = REQUEST_CODE_FINE_LOCATION,
          perms = *arrayOf(ACCESS_FINE_LOCATION)
      )
    }
  }

  @AfterPermissionGranted(REQUEST_CODE_FINE_LOCATION)
  private fun setupLocationChangeListener() { //이게 setUpdateLocationLister
    if (EasyPermissions.hasPermissions(this, ACCESS_FINE_LOCATION)) {
      val locationRequest = LocationRequest()
      locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
      locationRequest.interval = 5000 // 5000ms (5s)
      if (ActivityCompat.checkSelfPermission(
          this,
          ACCESS_FINE_LOCATION
        ) != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
          this,
          Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PERMISSION_GRANTED
      ) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
      }
      if (ActivityCompat.checkSelfPermission(
          this,
          ACCESS_FINE_LOCATION
        ) != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
          this,
          Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PERMISSION_GRANTED
      ) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
      }
      fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    } else {
      // Do not have permissions, request them now
      EasyPermissions.requestPermissions(
          host = this,
          rationale = "For showing your current location on the map.",
          requestCode = REQUEST_CODE_FINE_LOCATION,
          perms = *arrayOf(ACCESS_FINE_LOCATION)
      )
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
      grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when(requestCode){
      PERM_FLAG -> {
        var check = true
        for(grant in grantResults) {
          if(grant != PERMISSION_GRANTED) {
            check = false
            break
          }
        }
        if(check){
          startProcess()
        }else{
          Toast.makeText(this,"권한을 승인해야지만 앱을 사용할 수 있습니다.",Toast.LENGTH_LONG).show()
        }
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if(item.itemId == R.id.log_out) {
      FirebaseAuth.getInstance().signOut()
      val intent = Intent(this@MapsActivity,LoginActivity::class.java)
      startActivity(intent)
      finish()
      return true
    }
    return true

    }
  }

