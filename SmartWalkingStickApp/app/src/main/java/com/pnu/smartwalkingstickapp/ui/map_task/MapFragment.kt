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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.pnu.smartwalkingstickapp.R
import com.pnu.smartwalkingstickapp.databinding.FragmentMapBinding
import com.skt.Tmap.TMapView
import java.util.*

class MapFragment : Fragment(), TextToSpeech.OnInitListener {
    private lateinit var apiKey: String
    private lateinit var binding: FragmentMapBinding
    private lateinit var tts: TextToSpeech
    private var text: String = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        apply {
            apiKey = getString(R.string.TmapAPIKey)
        }
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMapView()
        initButtonClickListenerForTTS()
    }

    private fun initButtonClickListenerForTTS() {
        binding.constraintlayoutRoot.children.forEach { view ->
            view.setOnLongClickListener {
                if (view == view?.findViewById<Button>(R.id.btn_findPath)) {
                    Log.d("btn ", "길찾기 버튼")
                    val btn = view?.findViewById<Button>(R.id.btn_findPath)
                    text = btn!!.text.toString()
                }
                if (view == view?.findViewById<EditText>(R.id.etv_departure)) {
                    Log.d("btn ", "출발지 입력칸")
                    val btn = view?.findViewById<EditText>(R.id.etv_departure)
                    text = btn!!.text.toString()
                }
                if (view == view?.findViewById<EditText>(R.id.etv_destination)) {
                    Log.d("btn ", "도착지 입력칸")
                    val btn = view?.findViewById<EditText>(R.id.etv_destination)
                    text = btn!!.text.toString()
                }
                if (text != "") {
                    speakOut()
                    text = ""
                }
                return@setOnLongClickListener true
            }

        }
    }


    private fun initMapView() {
        val tMapView = TMapView(requireActivity())
        tMapView.setSKTMapApiKey(apiKey)
        binding.linearlayoutTMapView.addView(tMapView)

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun speakOut() {
        tts.setPitch(0.6F)
        tts.setSpeechRate(0.1F)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id1")
    }
    private fun initTextToSppech() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(activity, "SDK Version is low", Toast.LENGTH_SHORT).show()
            return
        }
        tts = TextToSpeech(activity)
    }

}