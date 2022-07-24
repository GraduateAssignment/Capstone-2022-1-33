package com.pnu.smartwalkingstickapp.ui.map_task

import android.Manifest
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
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.pnu.smartwalkingstickapp.R
import com.pnu.smartwalkingstickapp.databinding.FragmentMapBinding
import com.skt.Tmap.TMapView
import java.util.*


class MapFragment : Fragment() {
    private lateinit var apiKey: String
    private var binding: FragmentMapBinding? = null
    private lateinit var tts: TextToSpeech
    private var text: String = ""
    private val MY_PERMISSION_ACCESS_ALL = 100
    private lateinit var tMapView: TMapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        apply {
            apiKey = getString(R.string.TmapAPIKey)
        }
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initButtonLongClickListenerForTTS()
        initFindingDirectionButton()
    }

    private fun initFindingDirectionButton() {
        binding!!.btnFindPath.setOnClickListener{
            with(binding!!){
                val start = etvDeparture.text.toString()
                val dest = etvDestination.text.toString()
                val bundle = bundleOf("start" to start, "dest" to dest)
                findNavController().navigate(R.id.action_nav_map_fragment_to_showDirectionFragment,bundle)
            }
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
    

}