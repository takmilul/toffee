package com.banglalink.toffee.ui.splash

import android.database.sqlite.SQLiteDatabase
import android.media.MediaDrm
import android.os.Build
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiLoginService
import com.banglalink.toffee.apiservice.CheckForUpdateService
import com.banglalink.toffee.apiservice.CredentialService
import com.banglalink.toffee.apiservice.HeaderEnrichmentService
import com.banglalink.toffee.apiservice.ReportAppLaunch
import com.banglalink.toffee.data.network.response.HeaderEnrichmentResponse
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.CommonPreference.Companion.DRM_AVAILABLE
import com.banglalink.toffee.data.storage.CommonPreference.Companion.DRM_TIMEOUT
import com.banglalink.toffee.data.storage.CommonPreference.Companion.DRM_UNAVAILABLE
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.usecase.AdvertisingIdLogData
import com.banglalink.toffee.usecase.HeaderEnrichmentLogData
import com.banglalink.toffee.usecase.SendAdvertisingIdLogEvent
import com.banglalink.toffee.usecase.SendDrmFallbackEvent
import com.banglalink.toffee.usecase.SendDrmUnavailableLogEvent
import com.banglalink.toffee.usecase.SendHeaderEnrichmentLogEvent
import com.banglalink.toffee.usecase.SendLoginLogEvent
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import javax.inject.Inject

const val MAX_PROGRESS_VALUE: Int = 10_000
const val DRM_AVAILABILITY_TIMEOUT: Long = 1_000

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
    
    val apiLoadingProgress = SingleLiveEvent<Int>()
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
                is Success -> {
                    apiLoadingProgress.value = (MAX_PROGRESS_VALUE / 3) * 2
                    getAppLaunchConfig()
                }
                is Failure -> {
                    appLaunchConfigLiveData.value = response
                }
            }
        }
    }
    
    fun checkForUpdateStatus(isNewUser: Boolean) {
        viewModelScope.launch {
            val updateResponse = resultFromResponse { checkForUpdateService.execute(BuildConfig.VERSION_CODE.toString()) }
            if (updateResponse is Success && updateResponse.data?.isFromCache == false) {
                apiLoadingProgress.value = if (isNewUser) MAX_PROGRESS_VALUE / 3 else MAX_PROGRESS_VALUE / 2
            }
            updateStatusLiveData.value = updateResponse
        }
    }
    
    fun getAppLaunchConfig() {
        viewModelScope.launch {
            val response = resultFromResponse { apiLoginService.execute() }
            if (response is Success) {
                apiLoadingProgress.value = MAX_PROGRESS_VALUE
            }
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
                ToffeeAnalytics.logEvent(ToffeeEvents.APP_LAUNCH)
            } catch (e: Exception) {
                ToffeeAnalytics.logBreadCrumb("Exception in ReportAppLaunch")
            }
        }
    }
    
    fun deletePreviousDatabase() {
        runCatching {
            val data: File = Environment.getDataDirectory()
            val previousDBPath = "/data/com.banglalink.toffee/databases/" + "toffee_database"
            val previousDB = File(data, previousDBPath)
            if (previousDB.exists()) {
                mPref.isPreviousDbDeleted = SQLiteDatabase.deleteDatabase(previousDB)
            } else {
                mPref.isPreviousDbDeleted = true
            }
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
                    runCatching {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
                        }
                        val isSupported = MediaDrm.isCryptoSchemeSupported(C.WIDEVINE_UUID)
                        cPref.isDrmModuleAvailable = if (isSupported) DRM_AVAILABLE else DRM_UNAVAILABLE
                        if (!isSupported) {
                            sendDrmUnavailableLogEvent.execute()
                        }
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