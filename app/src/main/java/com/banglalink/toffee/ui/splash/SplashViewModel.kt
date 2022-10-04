package com.banglalink.toffee.ui.splash

import android.database.sqlite.SQLiteDatabase
import android.media.MediaDrm
import android.os.Build
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.*
import com.banglalink.toffee.data.network.response.HeaderEnrichmentResponse
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.CommonPreference.Companion.DRM_AVAILABLE
import com.banglalink.toffee.data.storage.CommonPreference.Companion.DRM_TIMEOUT
import com.banglalink.toffee.data.storage.CommonPreference.Companion.DRM_UNAVAILABLE
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.usecase.*
import com.banglalink.toffee.util.SingleLiveEvent
import com.google.android.exoplayer2.C
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject

const val DRM_AVAILABILITY_TIMEOUT: Long = 1000

@HiltViewModel
class SplashViewModel @Inject constructor(
    val mPref: SessionPreference,
    val cPref: CommonPreference,
    private val apiLoginService: ApiLoginService,
    private val credentialService: CredentialService,
    private val sendLoginLogEvent: SendLoginLogEvent,
    @AppCoroutineScope private val appScope: CoroutineScope,
    private val sendAdIdLogEvent: SendAdvertisingIdLogEvent,
    private val sendHeLogEvent: SendHeaderEnrichmentLogEvent,
    private val sendDrmFallbackLogEvent: SendDrmFallbackEvent,
    private val checkForUpdateService: CheckForUpdateService,
    private val headerEnrichmentService: HeaderEnrichmentService,
    private val sendDrmUnavailableLogEvent: SendDrmUnavailableLogEvent
) : ViewModel() {
    
    val updateStatusLiveData = SingleLiveEvent<Resource<Any?>>()
    val appLaunchConfigLiveData = SingleLiveEvent<Resource<Any>>()
    val headerEnrichmentLiveData = SingleLiveEvent<Resource<HeaderEnrichmentResponse>>()
    
    private val reportAppLaunch by lazy {
        ReportAppLaunch()
    }
    
    fun getCredential() {
        viewModelScope.launch {
            val response = resultFromResponse { credentialService.execute() }
            when (response) {
                is Resource.Success -> {
                    getAppLaunchConfig()
                }
                is Resource.Failure -> {
                    appLaunchConfigLiveData.value = response
                }
            }
        }
    }
    
    fun checkForUpdateStatus() {
        viewModelScope.launch {
            val updateResponse = resultFromResponse { checkForUpdateService.execute(BuildConfig.VERSION_CODE.toString()) }
            updateStatusLiveData.value = updateResponse
        }
    }
    
    fun getAppLaunchConfig() {
        viewModelScope.launch {
            val response = resultFromResponse { apiLoginService.execute() }
            appLaunchConfigLiveData.value = response
        }
    }
    
    fun getHeaderEnrichment() {
        viewModelScope.launch {
            val response = resultFromResponse { headerEnrichmentService.execute() }
            headerEnrichmentLiveData.value = response
        }
    }
    
    fun reportAppLaunch() {
        appScope.launch {
            try {
                reportAppLaunch.execute()
            } catch (e: Exception) {
                ToffeeAnalytics.logBreadCrumb("Exception in ReportAppLaunch")
            }
        }
    }
    
    fun deletePreviousDatabase() {
        try {
            val data: File = Environment.getDataDirectory()
            val previousDBPath = "/data/com.banglalink.toffee/databases/" + "toffee_database"
            val previousDB = File(data, previousDBPath)
            if (previousDB.exists()) {
                mPref.isPreviousDbDeleted = SQLiteDatabase.deleteDatabase(previousDB)
            } else {
                mPref.isPreviousDbDeleted = true
            }
        } catch (e: Exception) {
        }
    }
    
    fun sendLoginLogData() {
        appScope.launch {
            try {
                sendLoginLogEvent.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun sendHeLogData(heLogData: HeaderEnrichmentLogData) {
        appScope.launch {
            try {
                sendHeLogEvent.execute(heLogData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun sendDrmUnavailableLogData() {
        appScope.launch {
            cPref.isDrmModuleAvailable = CommonPreference.DRM_TIMEOUT
            withTimeout(DRM_AVAILABILITY_TIMEOUT) {
                withContext(Dispatchers.IO + Job()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        try {
                            val securityLevel = when (val level = MediaDrm.getMaxSecurityLevel()) {
                                MediaDrm.SECURITY_LEVEL_UNKNOWN -> MediaDrm.SECURITY_LEVEL_UNKNOWN.toString()
                                MediaDrm.SECURITY_LEVEL_SW_SECURE_CRYPTO -> MediaDrm.SECURITY_LEVEL_SW_SECURE_CRYPTO.toString()
                                MediaDrm.SECURITY_LEVEL_SW_SECURE_DECODE -> MediaDrm.SECURITY_LEVEL_SW_SECURE_DECODE.toString()
                                MediaDrm.SECURITY_LEVEL_HW_SECURE_CRYPTO -> MediaDrm.SECURITY_LEVEL_HW_SECURE_CRYPTO.toString()
                                MediaDrm.SECURITY_LEVEL_HW_SECURE_DECODE -> MediaDrm.SECURITY_LEVEL_HW_SECURE_DECODE.toString()
                                MediaDrm.SECURITY_LEVEL_HW_SECURE_ALL -> MediaDrm.SECURITY_LEVEL_HW_SECURE_ALL.toString()
                                else -> level.toString()
                            }
                            ToffeeAnalytics.logBreadCrumb(securityLevel)
                        } catch (e: Exception) {
                        }
                    }
                    MediaDrm.isCryptoSchemeSupported(C.WIDEVINE_UUID)
                }.also {
                    cPref.isDrmModuleAvailable = if (it) DRM_AVAILABLE else DRM_UNAVAILABLE
                    if (!it) {
                        sendDrmUnavailableLogEvent.execute()
                    }
                }
            }
        }
        appScope.launch {
            delay(DRM_AVAILABILITY_TIMEOUT + 500)
            withContext(Dispatchers.IO + Job()) {
                when (cPref.isDrmModuleAvailable) {
                    DRM_TIMEOUT -> "Drm timeout"
                    DRM_UNAVAILABLE -> "Drm module unavailable"
                    else -> null
                }?.let {
                    sendDrmFallbackLogEvent.execute(0, it)
                }
            }
        }
    }
    
    fun sendAdvertisingIdLogData(adIdData: AdvertisingIdLogData) {
        appScope.launch {
            try {
                sendAdIdLogEvent.execute(adIdData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}