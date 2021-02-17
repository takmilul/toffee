package com.banglalink.toffee.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.banglalink.toffee.ToffeeApplication
import com.banglalink.toffee.data.storage.PlayerPreference
import com.banglalink.toffee.usecase.ReportLastPlayerSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerViewModel(application: Application):AndroidViewModel(application) {

    private val applicationScope by lazy {
        val toffeeApplication = application as ToffeeApplication
        toffeeApplication.applicationScope
    }

    private val reportLastPlayerSession by lazy {
        ReportLastPlayerSession(PlayerPreference.getInstance())
    }

    fun reportBandWidthFromPlayerPref(durationInSec:Long,totalBytesInMB:Double){
        applicationScope.launch {
            withContext(Dispatchers.IO){
                PlayerPreference.getInstance().savePlayerSessionBandWidth(durationInSec,totalBytesInMB)
                reportLastPlayerSession.execute()
            }
        }
    }
}