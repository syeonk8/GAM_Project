package com.example.gam_project.fragment

import androidx.exifinterface.media.ExifInterface
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gam_project.others.Constants.calendar_date
import com.example.gam_project.others.Constants.polyline_color
import com.example.gam_project.MainActivity
import com.example.gam_project.entity.TrackingEntity
import com.example.gam_project.tracking.R
import com.example.gam_project.tracking.databinding.FragmentDailyBinding
import com.example.gam_project.viewmodel.DailyViewModel
import com.google.android.gms.maps.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_record_page.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.content.Context
import android.graphics.*
import android.widget.ImageView
import android.widget.Toast
import com.example.gam_project.others.Constants.image_view_state
import com.google.android.gms.maps.model.*
import splitties.views.bottomPadding
import splitties.views.leftPadding
import splitties.views.rightPadding

@AndroidEntryPoint
class DailyFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentDailyBinding? = null
    private val binding get() = _binding!!

    private val DailyViewModel: DailyViewModel by viewModels()

    private lateinit var googleMap: GoogleMap
    private var polylineOptions = PolylineOptions()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDailyBinding.inflate(inflater, container, false)
        polylineOptions.color(polyline_color)

        val activity = activity as MainActivity
        activity.hideBottomNavigation(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.date.text = String.format("날짜 : %s", calendar_date)

        DailyViewModel.calendarTotalDistanceTravelled.observe(viewLifecycleOwner){
            it ?: return@observe
            binding.totalDistanceTextView.text = String.format("하루 총 거리 : %.2fm", it)
        }

        DailyViewModel.calendarAllTrackingEntities.observe(viewLifecycleOwner){
            addLocationListToRoute(it)
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val view = binding.root

        return view
    }

    private fun addLocationListToRoute(trackingEntityList: List<TrackingEntity>) {
        if (!this::googleMap.isInitialized) {
            return
        }
        val builder = LatLngBounds.builder()
        if (trackingEntityList.isEmpty()) {
            binding.totalDistanceTextView.text = String.format("하루 총 거리 : 0m")
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

        DailyViewModel.getCalendarAllTrackingEntities(calendar_date)

        if (image_view_state) {
            getMediaOnMap()
        }

        val adapter = CustomInfoWindowAdapter(requireContext())
        googleMap.setInfoWindowAdapter(adapter)

        googleMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }
    }

    private fun getMediaOnMap() {
        val date = SimpleDateFormat("yyyy-MM-dd").parse(calendar_date)
        val startDate = Date(date.time)
        val endDate = Date(date.time + TimeUnit.DAYS.toMillis(1))

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
            Toast.makeText(requireContext(), "해당 날짜에 찍은 사진이 없거나 사진의 위치정보가 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
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
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
        val activity = activity as MainActivity
        activity.hideBottomNavigation(false)
    }
}