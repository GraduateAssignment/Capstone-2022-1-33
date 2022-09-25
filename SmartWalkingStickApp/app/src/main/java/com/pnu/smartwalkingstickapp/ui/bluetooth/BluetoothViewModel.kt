package com.pnu.smartwalkingstickapp.ui.bluetooth

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment
import com.pnu.smartwalkingstickapp.ui.map_task.MapFragment
import com.pnu.smartwalkingstickapp.ui.map_task.ShowDirectionFragment
import com.pnu.smartwalkingstickapp.ui.ocr_task.OcrFragment

class BluetoothViewModel(): ViewModel() {
    private var navHostFragment: NavHostFragment? = null

    private val _onReceiveRunEmergencyCall = MutableLiveData<Boolean>(false)
    val onReceiveRunEmergencyCall: LiveData<Boolean> = _onReceiveRunEmergencyCall

    private val _onReceiveRunCamera = MutableLiveData<Boolean>(false)
    val onReceiveRunCamera: LiveData<Boolean> = _onReceiveRunCamera

    fun runTask(tag: String) {
        val topFragment: Fragment? = navHostFragment?.childFragmentManager?.fragments?.get(0)
        val bundle = bundleOf("feature" to tag)
        when (topFragment) {
            is MapFragment -> (topFragment as MapFragment).actionToCameraXFragment(bundle)
            is OcrFragment -> (topFragment as OcrFragment).actionToCameraXFragment(bundle)
            is BluetoothFragment -> (topFragment as BluetoothFragment).actionToCameraXFragment(bundle)
            is ShowDirectionFragment ->(topFragment as ShowDirectionFragment).actionToCameraXFragment(bundle)
            else -> null
        }
    }

    fun runEmergencyCall() {
        _onReceiveRunEmergencyCall.postValue(true)
    }

    fun runCamera() {
        _onReceiveRunCamera.postValue(true)
    }

    fun navHostFragment(_navHostFragment: NavHostFragment){
        navHostFragment = _navHostFragment
    }
}