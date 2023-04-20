package com.example.gam_project.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.gam_project.entity.CalendarEntity
import com.example.gam_project.others.Constants
import com.example.gam_project.entity.TrackingEntity
import com.example.gam_project.tracking.databinding.FragmentEditPageBinding
import com.example.gam_project.viewmodel.EditPageViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditPageFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentEditPageBinding? = null
    private val binding get() = _binding!!

    private var entity: CalendarEntity? = null
    private val EditPageViewModel: EditPageViewModel by viewModels()

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    private var polylineOptions = PolylineOptions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            entity = it.getParcelable("entity")
        }

        savedInstanceState?.let {
            entity = it.getParcelable("entity")
        }

        assert(entity != null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPageBinding.inflate(inflater, container, false)
        polylineOptions.color(Constants.polyline_color)

        mapView = binding.mapView

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        val view = binding.root

        with(binding) {
            goBackButton.setOnClickListener {
                requireActivity().onBackPressed()
            }

            saveButton.setOnClickListener {
                lifecycleScope.launch {
                    val newEntity = CalendarEntity(
                        id = entity!!.id,
                        entity!!.routeId,
                        entity!!.startTime,
                        entity!!.endTime,
                        entity!!.year,
                        entity!!.month,
                        entity!!.day,
                        title.text.toString().trim(),
                        contents.text.toString().trim(),
                        ratingBar.rating
                    )

                    EditPageViewModel.update(newEntity)
                    requireActivity().onBackPressed()
                }
            }

            title.setText(entity!!.title)
            date.text = String.format("기록 시간 : %s - %s", entity!!.startTime, entity!!.endTime)
            ratingBar.rating = entity!!.rating
            contents.setText(entity!!.contents)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val routes = EditPageViewModel.getRoutes(entity!!.routeId)
            updateAllDisplayText(routes.map { it.distanceTravelled }.sum())
            addLocationListToRoute(routes)
            Constants.isEmpty = routes.isEmpty()
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

        val latitude = 37.5666805
        val longitude = 126.9784147
        val seoulLatLong = LatLng(latitude, longitude)

        val zoomLevel = 9.5f
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(seoulLatLong, zoomLevel))
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        mapView.onSaveInstanceState(outState)
        outState.putParcelable("entity", entity)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
