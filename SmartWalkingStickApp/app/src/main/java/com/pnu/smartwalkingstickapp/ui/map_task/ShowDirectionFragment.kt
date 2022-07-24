package com.pnu.smartwalkingstickapp.ui.map_task

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pnu.smartwalkingstickapp.R
import com.pnu.smartwalkingstickapp.databinding.FragmentShowDirectionBinding
import com.pnu.smartwalkingstickapp.ui.map_task.response.path.Feature
import com.pnu.smartwalkingstickapp.ui.map_task.response.path.FeatureCollection
import com.pnu.smartwalkingstickapp.ui.map_task.response.search.Poi
import com.pnu.smartwalkingstickapp.ui.map_task.utility.RetrofitUtil
import com.skt.Tmap.TMapData
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapView
import com.skt.Tmap.poi_item.TMapPOIItem
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.CoroutineContext


class ShowDirectionFragment : Fragment() , CoroutineScope, TextToSpeech.OnInitListener {

    private val mapViewModel : MapViewModel by activityViewModels()
    private var binding : FragmentShowDirectionBinding? = null
    private  lateinit var job : Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val TAG = "jiwoo"
    private lateinit var adapter: PathDataRecyclerViewAdapter

    private var tts : TextToSpeech? = null

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
        initRcvAdapter()
        getPathInformation()
    }

    private fun getPathInformation() {
        with(mapViewModel){
            launch(coroutineContext) {
                try{
                    withContext(Dispatchers.IO){
                        val response = RetrofitUtil.apiService.getPath(
                            startX = startPoi!!.frontLon, startY = startPoi!!.frontLat, startName = startPoi!!.name!!,
                            endX = destPoi!!.frontLon, endY = destPoi!!.frontLat, endName = destPoi!!.name!!
                        )
                        if(response.isSuccessful){
                            val body = response.body()
                            withContext(Dispatchers.Main){
                                Log.d(TAG, "${body!!.features}")
                                setData(body!!.features)
                            }
                        }
                    }
                }
                catch (e : Exception){
                }
            }
        }
    }
    private fun initRcvAdapter(){
        adapter = PathDataRecyclerViewAdapter()
        with(binding!!){
            rcvPathData.layoutManager = LinearLayoutManager(activity)
            rcvPathData.adapter = adapter
        }

    }

    private fun setData(featureList: List<Feature>) {
        Log.d(TAG, "setData: ${featureList.size}", )
        adapter.setData(featureList)
    }

    private fun initTextToSpeech(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            Toast.makeText(activity, "SDK version is low", Toast.LENGTH_SHORT).show()
            return
        }
        tts = TextToSpeech(requireContext(),this)


    }


    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }

    override fun onInit(p0: Int) {
        TODO("Not yet implemented")
    }
}