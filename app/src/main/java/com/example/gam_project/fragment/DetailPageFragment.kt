package com.example.gam_project.fragment

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.gam_project.entity.CalendarEntity
import com.example.gam_project.others.Constants.image_view_state
import com.example.gam_project.others.Constants.isEmpty
import com.example.gam_project.others.Constants.polyline_color
import com.example.gam_project.MainActivity
import com.example.gam_project.entity.TrackingEntity
import com.example.gam_project.tracking.R
import com.example.gam_project.tracking.databinding.FragmentDetailPageBinding
import com.example.gam_project.viewmodel.DetailPageViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import splitties.views.bottomPadding
import splitties.views.leftPadding
import splitties.views.rightPadding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class DetailPageFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDetailPageBinding? = null
    private val binding get() = _binding!!

    private var entity: CalendarEntity? = null
    private val DetailPageViewModel: DetailPageViewModel by viewModels()

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var polylineOptions = PolylineOptions()

    var startTime: String? = null
    var endTime: String? = null

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
        _binding = FragmentDetailPageBinding.inflate(inflater, container, false)
        polylineOptions.color(polyline_color)

        val activity = activity as MainActivity
        activity.hideBottomNavigation(true)

        mapView = binding.mapView

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        binding.goBackButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.editButton.setOnClickListener {
            val arguments = bundleOf("entity" to entity!!)
            findNavController().navigate(R.id.editPageFragment, arguments)
        }

        binding.deleteButton.setOnClickListener {
            lifecycleScope.launch {
                DetailPageViewModel.delete(entity!!)
            }
        }

        val view = binding.root

        DetailPageViewModel.getEntity(entity!!.id).observe(viewLifecycleOwner) {
            if (it == null) {
                requireActivity().onBackPressed()
                return@observe
            }

            this.entity = it

            with(binding) {
                title.text = it.title
                date.text = String.format("기록 시간 : %s - %s", it.startTime, it.endTime)
                ratingBar.rating = it.rating
                contents.text = it.contents
            }

            startTime = it.startTime
            endTime = it.endTime

            if (image_view_state) {
                getMediaOnMap(startTime!!, endTime!!)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val routes = DetailPageViewModel.getRoutes(entity!!.routeId)
            updateAllDisplayText(routes.map { it.distanceTravelled }.sum())
            addLocationListToRoute(routes)
            isEmpty = routes.isEmpty()
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
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoulLatLong, zoomLevel))

        val adapter = CustomInfoWindowAdapter(requireContext())
        googleMap.setInfoWindowAdapter(adapter)

        googleMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }
    }

    private fun getMediaOnMap(startTime: String, endTime: String) {
        val start_time = SimpleDateFormat("yyyy.MM.dd HH:mm").parse(startTime)
        val startDate = Date(start_time.time)
        val end_time = SimpleDateFormat("yyyy.MM.dd HH:mm").parse(endTime)
        val endDate = Date(end_time.time + TimeUnit.MINUTES.toMillis(1))

        val selection = "${MediaStore.Images.Media.DATE_TAKEN}>=? and ${MediaStore.Images.Media.DATE_TAKEN}<=?"
        val selectionArgs = arrayOf(startDate.time.toString(), endDate.time.toString())

        val cursor = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media.DATA),
            selection,
            selectionArgs,
            "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        )

        var latLng: DoubleArray?
        var foundImage = false

        while (cursor!!.moveToNext()) {
            val filePath = cursor.getString(0)
            val exifInterface = ExifInterface(filePath)
            latLng = exifInterface.getLatLong()

            if (latLng != null) {
                val markerOptions = MarkerOptions()
                    .position(LatLng(latLng[0], latLng[1]))

                val bitmap = BitmapFactory.decodeFile(filePath)
                val radius = 20f
                val borderSize = 10f

                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, false)

                val output = Bitmap.createBitmap(scaledBitmap.width, scaledBitmap.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(output)
                val paint = Paint().apply {
                    isAntiAlias = true
                    shader = BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                }
                val rect = RectF(0f, 0f, scaledBitmap.width.toFloat(), scaledBitmap.height.toFloat())
                canvas.drawRoundRect(rect, radius, radius, paint)

                val borderPaint = Paint().apply {
                    style = Paint.Style.STROKE
                    strokeWidth = borderSize
                    color = Color.WHITE
                }
                canvas.drawRoundRect(rect, radius, radius, borderPaint)

                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(output))

                val marker = googleMap.addMarker(markerOptions)
                marker.tag = filePath

                foundImage = true
            }
        }

        cursor.close()

        if (!foundImage) {
            Toast.makeText(requireContext(), "해당 시간에 찍은 사진이 없거나 사진의 위치정보가 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

        override fun getInfoWindow(marker: Marker): View? {
            val filePath = marker.tag as? String ?: return null
            val imageView = ImageView(context).apply {
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.CENTER_INSIDE

                val bitmap = BitmapFactory.decodeFile(filePath)
                setImageBitmap(bitmap)
            }

            imageView.leftPadding = 100
            imageView.rightPadding = 100
            imageView.bottomPadding = 10

            return imageView
        }

        override fun getInfoContents(marker: Marker): View? {
            return null
        }
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
        super.onDestroy()
        mapView.onDestroy()
        val activity = activity as MainActivity
        activity.hideBottomNavigation(false)
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
