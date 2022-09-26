package com.pnu.smartwalkingstickapp.ui.map_task

import android.util.Log
import androidx.lifecycle.ViewModel

class ShowDirectionViewModel : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        Log.d("JIWOO", "onCleared: ")
    }


    fun print() {
        Log.d("JIWOO", "print")
    }








}