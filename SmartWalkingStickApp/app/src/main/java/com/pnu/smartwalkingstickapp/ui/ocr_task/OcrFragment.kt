package com.pnu.smartwalkingstickapp.ui.ocr_task

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.pnu.smartwalkingstickapp.R
import com.pnu.smartwalkingstickapp.databinding.FragmentOcrBinding

class OcrFragment : Fragment() {
    private lateinit var binding: FragmentOcrBinding
    private lateinit var _cameraButton: Button
    private lateinit var _detectButton: Button
    private lateinit var TAG: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOcrBinding.inflate(inflater, container, false)
        _cameraButton = binding.cameraButton
        _detectButton = binding.detectButton
        TAG = arguments?.getString("tag").toString()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSetOnClickListener()
    }

    private fun initSetOnClickListener(){
        _cameraButton.setOnClickListener {
            val bundle = bundleOf("feature" to "text")
            findNavController().navigate(R.id.action_nav_ocr_fragment_to_nav_camera_x_fragment, bundle)
        }
        _detectButton.setOnClickListener {
            val bundle = bundleOf("feature" to "detect")
            findNavController().navigate(R.id.action_nav_ocr_fragment_to_nav_camera_x_fragment, bundle)
        }
    }

    fun actionToCameraXFragment(bundle: Bundle){
        findNavController().navigate(R.id.action_nav_ocr_fragment_to_nav_camera_x_fragment, bundle)
    }

}