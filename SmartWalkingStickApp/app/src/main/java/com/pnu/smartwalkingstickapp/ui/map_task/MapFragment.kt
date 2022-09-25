package com.pnu.smartwalkingstickapp.ui.map_task

import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pnu.smartwalkingstickapp.R
import com.pnu.smartwalkingstickapp.databinding.FragmentMapBinding
import com.pnu.smartwalkingstickapp.ui.map_task.response.search.Poi
import com.pnu.smartwalkingstickapp.ui.map_task.utility.RetrofitUtil
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class MapFragment : Fragment(), CoroutineScope {
    private val mapViewModel: MapViewModel by activityViewModels()

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var apiKey: String
    private lateinit var adapter: PoiDataRecyclerViewAdapter
    private var binding: FragmentMapBinding? = null
    private lateinit var tts: TextToSpeech
    private var text: String = ""

    private var state: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        apply {
            apiKey = getString(R.string.TmapAPIKey)
        }
        binding = FragmentMapBinding.inflate(inflater, container, false)
        job = Job()

        return binding!!.root
    }

    val TAG = "ABCDE"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Map onViewCreated: ")
        initButtonLongClickListenerForTTS()
        initFindingDirectionButton()
        initRcvAdapter()
        initButton()
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "MAP onDetach: ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "MAP onDestroyView: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MAP onDestroy: ")
    }

    private fun initFindingDirectionButton() {
        binding!!.btnFindPath.setOnClickListener {
            findNavController().navigate(R.id.action_nav_map_fragment_to_showDirectionFragment)
        }
    }

    private fun initButtonLongClickListenerForTTS() {
        binding!!.constraintlayoutRoot.children.forEach { view ->
            view.setOnLongClickListener {
                if (view == view.findViewById<Button>(R.id.btn_findPath)) {
                    Log.d("btn ", "길찾기 버튼")
                    val btn = view.findViewById<Button>(R.id.btn_findPath)
                    text = btn!!.text.toString()
                }
                if (view == view.findViewById<EditText>(R.id.etv_departure)) {
                    Log.d("btn ", "출발지 입력칸")
                    val btn = view.findViewById<EditText>(R.id.etv_departure)
                    text = btn!!.text.toString()
                }
                if (view == view.findViewById<EditText>(R.id.etv_destination)) {
                    Log.d("btn ", "도착지 입력칸")
                    val btn = view.findViewById<EditText>(R.id.etv_destination)
                    text = btn!!.text.toString()
                }
                if (text != "") {
                    // TODO : TTs 기능
                    text = ""
                }
                return@setOnLongClickListener true
            }

        }
    }

    private fun initButton() {
        with(binding!!) {
            btnStartPOI.setOnClickListener {
                state = "start"
                getPOIData(etvDeparture.text.toString())
            }
            btnDestPOI.setOnClickListener {
                state = "dest"
                getPOIData(etvDestination.text.toString())
            }
        }
    }

    private fun initRcvAdapter() {
        adapter = PoiDataRecyclerViewAdapter().apply {
            setOnStateInterface(object : OnPoiDataItemClick {
                override fun sendData(item: Poi) {
                    when (state) {
                        "start" -> {
                            mapViewModel.startPoi = item
                            binding!!.etvDeparture.setText(item.name)
                        }
                        "dest" -> {
                            mapViewModel.destPoi = item
                            binding!!.etvDestination.setText(item.name)
                        }

                    }
                }
            })
        }
        with(binding!!) {
            rcvPoiData.layoutManager = LinearLayoutManager(activity)
            rcvPoiData.adapter = adapter
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun speakOut() {
        tts.setPitch(0.6F)
        tts.setSpeechRate(0.1F)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id1")
    }

    private fun setPoiData(poiList: List<Poi>) {
        adapter.setData(poiList.toMutableList())
    }

    private fun getPOIData(keyword: String) {
        launch(coroutineContext) {
            try {
                withContext(Dispatchers.IO) {
                    val response = RetrofitUtil.apiService.getSearchLocation(
                        keyword = keyword
                    )
                    if (response.isSuccessful) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            setPoiData(body!!.searchPoiInfo.pois.poi)
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }


    }
}