package com.example.gam_project.fragment

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.gam_project.others.Constants.ISTRACKING
import com.example.gam_project.others.Constants.polyline_color
import com.example.gam_project.others.Constants.today_date
import com.example.gam_project.others.Constants.ID
import com.example.gam_project.entity.TrackingEntity
import com.example.gam_project.others.Constants
import com.example.gam_project.others.TrackingUtility
import com.example.gam_project.service.TrackingService
import com.example.gam_project.tracking.R
import com.example.gam_project.tracking.databinding.FragmentMapsBinding
import com.example.gam_project.viewmodel.MapsViewModel
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@AndroidEntryPoint
class MapsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap //구글 맵
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient //현재위치

    private var polyline: Polyline? = null
    private var polylineOptions = PolylineOptions()

    // 뷰모델
    private val MapsViewModel: MapsViewModel by viewModels()
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    // Route ID
    //private var id: String = "";
    private var locationCallback: LocationCallback? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        today_date = LocalDate.now().toString()
        //setPolylineColor()
        polylineOptions.color(polyline_color)


        startProcess()
        updateButtonStatus()

        // 현재위치
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        // 시작 버튼 클릭시
        binding.startButton.setOnClickListener {
            //mMap.clear()
            ISTRACKING = true

            //백그라운드/포그라운드 동작
            sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)

            updateButtonStatus()

            // 이동거리 초기화
            updateAllDisplayText(0f)

            //트래킹 시작
            ID = UUID.randomUUID().toString();
            MapsViewModel.getIDTotalDistanceTravelled(ID).observe(viewLifecycleOwner) {
                it ?: return@observe
                updateAllDisplayText(it)
            }

            updateLocationTracking()
        }
        //종료버튼 클릭시
        binding.endButton.setOnClickListener {

            //서비스 종료
            sendCommandToService(Constants.ACTION_STOP_SERVICE)
            // Update layouts
            updateButtonStatus()
            //종료가 맞는지 확인
            endButtonClicked()
        }

        //viewmodel
//        MapsViewModel.allTrackingEntities.observe(viewLifecycleOwner) { allTrackingEntities ->
//            if (allTrackingEntities.isEmpty()) {
//                updateAllDisplayText(0f)
//            }
//        }

        lifecycleScope.launch {
            val routes = MapsViewModel.getRoutes(ID)
//            updateAllDisplayText(routes.map { it.distanceTravelled }.sum())
            addLocationListToRoute(routes)
            if (routes.isEmpty()) {
                updateAllDisplayText(0f)
            }
        }

        MapsViewModel.lastTrackingEntity.observe(viewLifecycleOwner) { lastTrackingEntity ->
            lastTrackingEntity ?: return@observe
            addLocationToRoute(lastTrackingEntity)
        }

        MapsViewModel.getIDTotalDistanceTravelled(ID).observe(viewLifecycleOwner) {
            it ?: return@observe
            updateAllDisplayText(it)
        }

//        MapsViewModel.todayTotalDistanceTravelled.observe(viewLifecycleOwner) {
//            it ?: return@observe
//            updateAllDisplayText(it)
//        }

//        MapsViewModel.allTrackingEntitiesRecord.observe(viewLifecycleOwner) {
//            addLocationListToRoute(it)
//        }

//        MapsViewModel.allTodayTrackingEntities.observe(viewLifecycleOwner) {
//            addLocationListToRoute(it)
//        }

        if (ISTRACKING) {
            updateLocationTracking()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        polyline = mMap.addPolyline(polylineOptions)

        showUserLocation()

        // 마커를 서울 시청으로 설정
        val latitude = 37.5666805
        val longitude = 126.9784147
        val seoulLatLong = LatLng(latitude, longitude)

        val zoomLevel = 9.5f
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(seoulLatLong, zoomLevel))

        // polyline 그리기
//        if (ISTRACKING) {
//            MapsViewModel.getTodayAllTrackingEntities(today_date)
//        }
    }

    fun startProcess() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    @SuppressLint("MissingPermission")
    private fun updateLocationTracking() {
        if (ISTRACKING) {
            if (locationCallback != null) return;

            if (TrackingUtility.hasLocationPermissions(requireContext())) {
                val request = LocationRequest().apply {
                    interval = 5000L
                    fastestInterval = 2000L
                    priority = PRIORITY_HIGH_ACCURACY
                }

                //id = UUID.randomUUID().toString();

                //현재위치 및 내부 db에 저장
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        super.onLocationResult(locationResult)

                        locationResult ?: return
                        locationResult.locations.forEach {
                            var currentTime = LocalDateTime.now()
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            val trackingEntity = TrackingEntity(
                                Calendar.getInstance().timeInMillis,
                                ID,
                                currentTime.format(formatter),
                                it.latitude,
                                it.longitude
                            )
                            MapsViewModel.insert(trackingEntity)
                            Log.v(
                                "NEW_LOCATION",
                                it.latitude.toString() + ", " + it.longitude.toString()
                            ) //log로 확인
//                Log.v("ISTRACKING",ISTRACKING.toString())
                        }
                    }
                }

                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            if (locationCallback != null) {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                locationCallback = null;
            }
        }
    }

    // Location
    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(1)
    private fun showUserLocation() {
        if (EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mMap.isMyLocationEnabled = true
        } else {
            EasyPermissions.requestPermissions(
                host = this,
                rationale = "For showing your current location on the map.",
                requestCode = 1,
                perms = *arrayOf(ACCESS_FINE_LOCATION)
            )
        }
    }

    // UI related codes
    private fun updateButtonStatus() {
        binding.startButton.isEnabled = !ISTRACKING
        binding.endButton.isEnabled = ISTRACKING
    }

    private fun updateAllDisplayText(totalDistanceTravelled: Float) {
        binding.totalDistanceTextView.text = String.format("총 거리: %.2fm", totalDistanceTravelled)
    }

    private fun stopTracking() {
        polylineOptions = PolylineOptions()

//        MapsViewModel.deleteDailyRoute()
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            locationCallback = null;
        }
    }

    private fun endButtonClicked() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("기록을 종료 하시겠습니까?")
        builder.setPositiveButton("확인") { _, _ ->
            ISTRACKING = false
            updateButtonStatus()
            stopTracking()
//            val fragment = RecordPageFragment()
//            val transaction = requireActivity().supportFragmentManager.beginTransaction()
//            transaction.replace(R.id.navHostFragment, fragment)
//            transaction.commit()

            val arguments = bundleOf(
                "id" to ID,
            )

            ID = ""
            findNavController().navigate(R.id.recordPageFragment, args = arguments)
        }

        builder.setNegativeButton("취소") { _, _ ->
        }

        builder.create()
        builder.show()
    }

    private fun addLocationListToRoute(trackingEntityList: List<TrackingEntity>) { //
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

    private fun addLocationToRoute(trackingEntity: TrackingEntity) { //새로
        if (!this::mMap.isInitialized) {
            return
        }
        mMap.clear()
        val newLatLngInstance = trackingEntity.asLatLng()
        polylineOptions.points.add(newLatLngInstance)
        mMap.addPolyline(polylineOptions)
    }

    //서비스 관련
    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        map?.onResume()
    }

    override fun onStart() {
        super.onStart()
        map?.onStart()
    }

    override fun onStop() {
        super.onStop()
        map?.onStop()
    }

    override fun onPause() {
        super.onPause()
        map?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map?.onLowMemory()
    }

    override fun onDestroy() {
        map?.onDestroy()

        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            locationCallback = null;

            // 강제 종료한 경우 (Route 와 관련된 CalendarEntry 가 없는 경우) Route 삭제
//            lifecycleScope.launch {
//                MapsViewModel.deleteRoutes(id)
//            }
        }

        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map?.onSaveInstanceState(outState)
    }
}
