package com.pnu.smartwalkingstickapp.ui.ocr_task

import android.content.Context
import android.graphics.Camera
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.pnu.smartwalkingstickapp.MainActivity
import com.pnu.smartwalkingstickapp.R
import com.pnu.smartwalkingstickapp.databinding.FragmentOcrBinding

class OcrFragment : Fragment() {
    private lateinit var binding: FragmentOcrBinding
    private lateinit var _cameraButton: Button
    private lateinit var _context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOcrBinding.inflate(inflater, container, false)
        _cameraButton = binding.cameraButton
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSetOnClickListener()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _context = context as MainActivity
    }

    private fun initSetOnClickListener(){
        _cameraButton.setOnClickListener {
            findNavController().navigate(R.id.action_nav_ocr_fragment_to_nav_camera_x_fragment)
        }
    }

}