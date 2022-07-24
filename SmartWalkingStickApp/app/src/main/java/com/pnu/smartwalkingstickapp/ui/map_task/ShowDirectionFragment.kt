package com.pnu.smartwalkingstickapp.ui.map_task

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.pnu.smartwalkingstickapp.R
import com.pnu.smartwalkingstickapp.databinding.FragmentShowDirectionBinding
import com.skt.Tmap.TMapData
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapView
import com.skt.Tmap.poi_item.TMapPOIItem


class ShowDirectionFragment : Fragment() {
    companion object {
        const val SEARCH_RESULT_EXTRA_KEY = "SearchResult"
        const val PERMISSION_REQUEST_CODE = 1
        const val CAMERA_ZOOM_LEVEL = 17f
    }

    private var binding: FragmentShowDirectionBinding? = null
    private lateinit var locationManager: LocationManager
    private lateinit var myLocationListener: MyLocationListener
    private lateinit var tMapView : TMapView
    private lateinit var start : String
    private lateinit var destination : String

    val TAG = "jiwoo"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for requireContext fragment

        binding = FragmentShowDirectionBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.apply {
            start = arguments?.getString("start").toString()
            destination = arguments?.getString("dest").toString()
        }
        initSetCurLocationToMapButton()
        getStartAndDestination()
        //Log.d(TAG, "onViewCreated: ${arguments?.getString("start")} ${arguments?.getString("dest")}")
    }

    private fun getStartAndDestination() {
        getPOIPathData(start,destination)
    }

    private fun getPOIPathData(start: String, destination: String) {
        val tMapData = TMapData()
        val arrTMapPoint: ArrayList<TMapPoint> = ArrayList()
        val arrTitle: ArrayList<String> = ArrayList()
        val arrAddress: ArrayList<String> = ArrayList()
    }


    private fun initSetCurLocationToMapButton() {
        binding!!.btnSetCurLocationToMap.setOnClickListener{
            getMyLocation()
        }
    }

    private fun getMyLocation() {
        if (::locationManager.isInitialized.not()) {
            locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        val isGpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (isGpsEnable) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                setMyLocationListener()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocationListener() {
        val minTime: Long = 1500
        val minDistance = 100f
        if (::myLocationListener.isInitialized.not()) {
            myLocationListener = MyLocationListener()
        }
        with(locationManager) {
            requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime, minDistance, myLocationListener
            )
            requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTime, minDistance, myLocationListener
            )
        }
    }

    inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            val curLocation = Pair(
                location.latitude,
                location.longitude
            )
            onCurrentLocationChanged(curLocation)
        }
    }
    private fun onCurrentLocationChanged(curLocation: Pair<Double,Double>) {
        Log.d("curLocation ", curLocation.first.toString() + "," + curLocation.second.toString())
        tMapView.setCenterPoint(curLocation.first, curLocation.second )
    }
    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}