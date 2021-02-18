package com.banglalink.toffee.ui.player

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.ToffeeApplication
import com.banglalink.toffee.data.storage.PlayerPreference
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.usecase.ReportLastPlayerSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerViewModel @ViewModelInject constructor(@AppCoroutineScope private val appScope: CoroutineScope):ViewModel() {

    private val reportLastPlayerSession by lazy {
        ReportLastPlayerSession(PlayerPreference.getInstance())
    }

    fun reportBandWidthFromPlayerPref(durationInSec:Long,totalBytesInMB:Double){
        appScope.launch {
            withContext(Dispatchers.IO){
                PlayerPreference.getInstance().savePlayerSessionBandWidth(durationInSec,totalBytesInMB)
                reportLastPlayerSession.execute()
            }
        }
    }
}