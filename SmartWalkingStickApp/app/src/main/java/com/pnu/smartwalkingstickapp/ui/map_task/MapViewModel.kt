package com.pnu.smartwalkingstickapp.ui.map_task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnu.smartwalkingstickapp.ui.map_task.response.search.Poi
import com.pnu.smartwalkingstickapp.ui.map_task.utility.RetrofitUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class MapViewModel : ViewModel() {
    var startPoi : Poi? = null
    var destPoi : Poi? = null


}