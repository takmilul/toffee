package com.banglalink.toffee.ui.player

import android.util.Log
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.apiservice.DrmTokenService
import com.banglalink.toffee.apiservice.ReportLastPlayerSession
import com.banglalink.toffee.data.storage.PlayerPreference
import com.banglalink.toffee.di.AppCoroutineScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @AppCoroutineScope private val appScope: CoroutineScope,
) : ViewModel() {

    private val reportLastPlayerSession by lazy {
        ReportLastPlayerSession(PlayerPreference.getInstance())
    }

    fun reportBandWidthFromPlayerPref(durationInSec: Long, totalBytesInMB: Double) {
        appScope.launch {
            withContext(Dispatchers.IO) {
                PlayerPreference.getInstance().savePlayerSessionBandWidth(durationInSec, totalBytesInMB)
                reportLastPlayerSession.execute()
            }
        }
    }
}