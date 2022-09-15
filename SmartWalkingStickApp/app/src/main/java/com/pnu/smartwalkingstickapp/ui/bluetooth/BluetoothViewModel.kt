package com.pnu.smartwalkingstickapp.ui.bluetooth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothViewModel : ViewModel() {

    private var _bluetoothState = MutableLiveData<String>()
    val bluetoothState: LiveData<String> get() = _bluetoothState
    
    fun setBluetoothState(newState : String) {
        _bluetoothState.value = newState
    }
}