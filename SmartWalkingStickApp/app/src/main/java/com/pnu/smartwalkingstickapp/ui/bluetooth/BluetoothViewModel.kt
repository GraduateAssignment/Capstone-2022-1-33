package com.pnu.smartwalkingstickapp.ui.bluetooth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothViewModel() : ViewModel() {

    private val _onReceiveRunEmergencyCall = MutableLiveData<Boolean>(false)
    val onReceiveRunEmergencyCall: LiveData<Boolean> = _onReceiveRunEmergencyCall

    private val _onReceiveRunCamera = MutableLiveData<String>("")
    val onReceiveRunCamera: LiveData<String> = _onReceiveRunCamera


    fun runEmergencyCall() {
        _onReceiveRunEmergencyCall.postValue(true)
    }

    fun runCamera(feature: String) {
        _onReceiveRunCamera.postValue(feature)
    }

}