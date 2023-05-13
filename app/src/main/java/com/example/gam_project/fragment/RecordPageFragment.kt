package com.example.gam_project.fragment

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.gam_project.entity.CalendarEntity
import com.example.gam_project.others.Constants.isEmpty
import com.example.gam_project.others.Constants.polyline_color
import com.example.gam_project.others.Constants.today_date
import com.example.gam_project.MainActivity
import com.example.gam_project.entity.TrackingEntity
import com.example.gam_project.tracking.R
import com.example.gam_project.tracking.databinding.FragmentRecordPageBinding
import com.example.gam_project.viewmodel.RecordPageViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_record_page.*
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class RecordPageFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentRecordPageBinding? = null
    private val binding get() = _binding!!

    private val RecordPageViewModel: RecordPageViewModel by viewModels()

    private var id: String = ""
    private lateinit var googleMap: GoogleMap
    private var polylineOptions = PolylineOptions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            id = it.getString("id", "")
        }

        savedInstanceState?.let {
            id = it.getString("id", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecordPageBinding.inflate(inflater, container, false)
        polylineOptions.color(polyline_color)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                return
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        val activity = activity as MainActivity
        activity.hideBottomNavigation(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val view = binding.root

        lifecycleScope.launch {
            val routes = RecordPageViewModel.getRoutes(id)
            updateAllDisplayText(routes.map { it.distanceTravelled }.sum())
            addLocationListToRoute(routes)
            isEmpty = routes.isEmpty()
        }

        val start_time_L = RecordPageViewModel.getStartTime(id)
        val end_time_L = RecordPageViewModel.getEndTime(id)
        val start_time = converterTime(start_time_L)
        val end_time = converterTime(end_time_L)

        binding.date.text = String.format("기록 시간 : %s - %s", start_time, end_time)

        binding.saveButton.setOnClickListener {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val day = cal.get(Calendar.DATE)

            val s_title = binding.title.text.toString()
            val s_contents = binding.contents.text.toString()
            val i_rating = binding.ratingBar.rating
            val calendarEntity = CalendarEntity(
                routeId = id,
                startTime = start_time,
                endTime = end_time,
                year = year,
                month = month,
                day = day,
                title = s_title,
                contents = s_contents,
                rating = i_rating
            )
            RecordPageViewModel.insert(calendarEntity)

//            val fragment = CalendarFragment()
//            val transaction = requireActivity().supportFragmentManager.beginTransaction()
//            transaction.replace(R.id.navHostFragment, fragment)
//            transaction.commit()
            findNavController().navigate(R.id.action_global_mapsFragment)
        }
        return view
    }

    private fun updateAllDisplayText(totalDistanceTravelled: Float) {
        binding.totalDistance.text = String.format("총 거리: %.2fm", totalDistanceTravelled)
    }

    private fun addLocationListToRoute(trackingEntityList: List<TrackingEntity>) {
        if (!this::googleMap.isInitialized) {
            return
        }
        googleMap.clear()
        val builder = LatLngBounds.builder()
        if (trackingEntityList.isEmpty()) {
            val latitude = 37.5666805
            val longitude = 126.9784147
            val seoulLatLong = LatLng(latitude, longitude)
            builder.include(seoulLatLong)
        } else {
            trackingEntityList.forEach { trackingEntity ->
                val newLatLngInstance = trackingEntity.asLatLng()
                polylineOptions.points.add(newLatLngInstance)
                builder.include(newLatLngInstance)
//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLngInstance, 15f))
            }
        }
        googleMap.addPolyline(polylineOptions)
        val bounds = builder.build()
        val padding = 100
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        googleMap.animateCamera(cu)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        // 마커를 서울 시청으로 설정
        val latitude = 37.5666805
        val longitude = 126.9784147
        val seoulLatLong = LatLng(latitude, longitude)

        val zoomLevel = 9.5f
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(seoulLatLong, zoomLevel))
    }

    private fun converterTime (time: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return simpleDateFormat.format(time)
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

    override fun onDestroy() {
        super.onDestroy()
        map?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        map?.onSaveInstanceState(outState)
        outState.putString("id", id)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
        val activity = activity as MainActivity
        activity.hideBottomNavigation(false)
    }
}