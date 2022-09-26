package com.pnu.smartwalkingstickapp.ui.map_task

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.pnu.smartwalkingstickapp.R
import com.pnu.smartwalkingstickapp.databinding.FragmentShowDirectionBinding
import com.pnu.smartwalkingstickapp.ui.map_task.response.path.Feature
import com.pnu.smartwalkingstickapp.ui.map_task.utility.Key
import com.pnu.smartwalkingstickapp.ui.map_task.utility.RetrofitUtil
import com.skt.Tmap.TMapGpsManager
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapPolyLine
import com.skt.Tmap.TMapView
import kotlinx.coroutines.*
import java.lang.Math.toRadians
import kotlin.coroutines.CoroutineContext
import kotlin.math.pow


class ShowDirectionFragment : Fragment(), CoroutineScope, TextToSpeech.OnInitListener {

    private val REQUEST_PERMISSION_LOCATION = 101


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tMapView: TMapView
    private lateinit var tMapGps: TMapGpsManager
    private var navigatePosition = 0

    private val mapViewModel: MapViewModel by activityViewModels()
    private val showDirectionViewModel: ShowDirectionViewModel by viewModels()

    private var binding: FragmentShowDirectionBinding? = null

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val TAG = "jiwoo"

    private var isNavigating: Boolean = false
    private lateinit var adapter: PathDataRecyclerViewAdapter

    private var tts: TextToSpeech? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for requireContext fragment
        job = Job()
        binding = FragmentShowDirectionBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        initRcvAdapter()
        getPathInformation()
        initNavigateButton()
        showDirectionViewModel.print()
    }

    private fun initMapView() {
        tMapView = TMapView(requireContext())
        tMapView.setSKTMapApiKey(Key.TMAP_API)

        tMapView.zoomLevel = 17;
        tMapView.setIconVisibility(true);
        tMapView.mapType = TMapView.MAPTYPE_STANDARD
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN)

        binding!!.linearLayoutMap.addView(tMapView)

        // Request For GPS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION);
        }

        // GPS using T Map
        tMapGps = TMapGpsManager(requireContext());

        // Initial Setting
        tMapGps.minTime = 1000
        tMapGps.minDistance = 10F
        tMapGps.provider = "network";

        tMapView.setCenterPoint(
            mapViewModel.startPoi!!.frontLon.toDouble(),
            mapViewModel.startPoi!!.frontLat.toDouble()
        )
        tMapView.setLocationPoint(
            mapViewModel.startPoi!!.frontLon.toDouble(),
            mapViewModel.startPoi!!.frontLat.toDouble()
        )

        tMapGps.OpenGps();
        Log.d(TAG, "initMapView: ${tMapGps.location.longitude} ${tMapGps.location.latitude}")
    }

    private fun initNavigateButton() {
        binding!!.btnStartNavigate.setOnClickListener {
            if (!isNavigating) {
                startNavigating()
                binding!!.btnStartNavigate.text = "길안내 종료"
            } else {
                endNavigating()
                binding!!.btnStartNavigate.text = "길안내 시작"
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "checkPermissionForLocation() 권한 상태 : O")
                true
            } else {
                // 권한이 없으므로 권한 요청 알림 보내기
                Log.d(TAG, "checkPermissionForLocation() 권한 상태 : X")
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION
                )
                false
            }
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult: requestCode : $requestCode")
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            Log.d(TAG, "onRequestPermissionsResult()")
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult()2")
                initMapView()
                setMapPolyLine(adapter.dataSet)
            } else {
                Log.d(TAG, "onRequestPermissionsResult() _ 권한 허용 거부")
            }
        } else {
            Log.d(TAG, "onRequestPermissionsResult() _ 권한 허용 거부123")
        }
    }

    private fun getPathInformation() {
        with(mapViewModel) {
            launch(coroutineContext) {
                try {
                    withContext(Dispatchers.IO) {
                        val response = RetrofitUtil.apiService.getPath(
                            startX = startPoi!!.frontLon,
                            startY = startPoi!!.frontLat,
                            startName = startPoi!!.name!!,
                            endX = destPoi!!.frontLon,
                            endY = destPoi!!.frontLat,
                            endName = destPoi!!.name!!
                        )
                        if (response.isSuccessful) {
                            val body = response.body()
                            withContext(Dispatchers.Main) {
                                setData(body!!.features)
                                if (adapter.dataSet.isNotEmpty()) {
                                    if (checkLocationPermission()) {
                                        initMapView()
                                        setMapPolyLine(adapter.dataSet)
                                    } else {
                                    }
                                } else {
                                    Log.d(TAG, "initNavigateButton: empty")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "$e")
                }
            }
        }
    }

    private fun setMapPolyLine(features: List<Feature>) {
        Log.d(TAG, "setMapPolyLine: ")
        val line = TMapPolyLine()
        features.forEach { feature ->
            val coordinate = getCoordinate(feature)!!
            line.addLinePoint(TMapPoint(coordinate.first, coordinate.second))
        }
        line.id = "line1"
        line.lineColor = Color.BLUE
        line.lineWidth = 2.0F
        tMapView.addTMapPolyLine(line.id, line)
    }

    private fun initRcvAdapter() {
        binding!!.rcvPathData.isNestedScrollingEnabled = false
        adapter = PathDataRecyclerViewAdapter()
        with(binding!!) {
            rcvPathData.layoutManager = LinearLayoutManager(activity)
            rcvPathData.adapter = adapter
        }

    }

    private fun setData(featureList: List<Feature>) {
        Log.d(TAG, "setData: ${featureList.size}")
        adapter.setData(featureList)
        Log.d(TAG, "setData: $featureList")
        //adapter.setData(featureList.filter { it.properties.pointType == "GP" })
    }

    @SuppressLint("MissingPermission")
    private fun startNavigating() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "startNavigatings() 두 위치 권한중 하나라도 없는 경우 ")
            return
        }
        isNavigating = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val mLocationRequest = LocationRequest.create().apply {
            interval = 3000 // 업데이트 간격 단위(밀리초)
            fastestInterval = 3000 // 가장 빠른 업데이트 간격 단위(밀리초)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // 정확성
            maxWaitTime = 5000 // 위치 갱신 요청 최대 대기 시간 (밀리초)
        }
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // 시스템에서 받은 location 정보를 onLocationChanged()에 전달
            val location = locationResult.lastLocation!!
            tMapView.setLocationPoint(location!!.longitude, location.latitude)
            tMapView.setCenterPoint(location.longitude, location.latitude)
            announceNavigate(location.latitude, location.longitude)
        }
    }

    private fun getCoordinate(feature: Feature): Pair<Double, Double>? {
        return when (feature.geometry.type) {
            "LineString" -> {
                val temp = feature.geometry.coordinates as List<*>
                val t = temp[0] as List<*>
                val lon = t[0] as Double
                val lat = t[1] as Double
                Pair(lat, lon)
            }
            "Point" -> {
                val temp = feature.geometry.coordinates as List<*>
                val lon = temp[0] as Double
                val lat = temp[1] as Double
                Pair(lat, lon)
            }
            else -> {
                null
            }
        }
    }

    private fun announceNavigate(newCurLon: Double, newCurLat: Double) {
        if (navigatePosition < adapter.dataSet.size) {
            val destFeature = adapter.dataSet[navigatePosition] // 현재 가려고 하는 경유지
            val destPos = getCoordinate(destFeature) // 현재 가려고 하는 경유지의 좌표
            val distance = getDistance(Pair(newCurLon, newCurLat), destPos!!)
            if (distance < 2) {
                Log.e(TAG, "approach ${destFeature.properties.description} ")
                Toast.makeText(
                    requireContext(),
                    "${adapter.dataSet[navigatePosition]} $distance",
                    Toast.LENGTH_SHORT
                ).show()
                navigatePosition++
            } else {
                Log.d(
                    TAG,
                    "destFeature : ${destFeature.properties.description}  distance : $distance"
                )
            }
        } else {
            navigatePosition = 0
            Toast.makeText(requireContext(), "목적지에 도착했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDistance(start: Pair<Double, Double>, end: Pair<Double, Double>): Int {
        Log.d(TAG, "start $start end $end")
        val R = 6372.8 * 1000
        val dLat = toRadians(end.second - start.second)
        val dLon = toRadians(end.first - start.first)
        val a =
            kotlin.math.sin(dLat / 2).pow(2.0) + kotlin.math.sin(dLon / 2)
                .pow(2.0) * kotlin.math.cos(
                toRadians(start.second)
            ) * kotlin.math.cos(
                toRadians(end.second)
            )
        val c = 2 * kotlin.math.asin(kotlin.math.sqrt(a))
        return (R * c).toInt()
    }


    private fun initTextToSpeech() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(activity, "SDK version is low", Toast.LENGTH_SHORT).show()
            return
        }
        tts = TextToSpeech(requireContext(), this)


    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onInit(p0: Int) {
        TODO("Not yet implemented")
    }

    private fun endNavigating() {
        if (isNavigating) {
            fusedLocationClient.removeLocationUpdates(mLocationCallback)
            isNavigating = false
        }
    }


    override fun onStop() {
        super.onStop()
        // 위치 업데이터를 제거 하는 메서드
        // 지정된 위치 결과 리스너에 대한 모든 위치 업데이트를 제거
        endNavigating()
    }

    fun actionToCameraXFragment(bundle: Bundle) {
        findNavController().navigate(
            R.id.action_showDirectionFragment_to_nav_camera_x_fragment,
            bundle
        )
    }
}
