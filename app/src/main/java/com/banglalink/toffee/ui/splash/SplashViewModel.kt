package com.banglalink.toffee.ui.splash

import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.*
import com.banglalink.toffee.data.network.response.HeaderEnrichmentResponse
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.usecase.HeaderEnrichmentLogData
import com.banglalink.toffee.usecase.SendHeaderEnrichmentLogEvent
import com.banglalink.toffee.usecase.SendLoginLogEvent
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    val mPref: SessionPreference,
    private val apiLogin: ApiLogin,
    private val checkUpdate: CheckUpdate,
    private val credential: CredentialService,
    private val sendLoginLogEvent: SendLoginLogEvent,
    @AppCoroutineScope private val appScope: CoroutineScope,
    private val sendHeLogEvent: SendHeaderEnrichmentLogEvent,
    private val headerEnrichmentService: HeaderEnrichmentService,
) : ViewModel() {

    val apiLoginResponse = SingleLiveEvent<Resource<Any>>()
    val headerEnrichmentResponse = SingleLiveEvent<Resource<HeaderEnrichmentResponse>>()
    
    private val reportAppLaunch by lazy {
        ReportAppLaunch()
    }
    
    fun credentialResponse() {
        viewModelScope.launch {
            val response = resultFromResponse { credential.execute() }
            when(response){
                is Resource.Failure -> {
                    apiLoginResponse.value = response
                }
                is Resource.Success -> {
                    loginResponse(false)
                }
            }
        }
    }
    
    fun loginResponse(skipUpdate: Boolean = false) {
        viewModelScope.launch {
            val response = resultFromResponse { apiLogin.execute() }
            if (!skipUpdate) {
                val updateResponse = resultFromResponse { checkUpdate.execute(BuildConfig.VERSION_CODE.toString())}
                if(updateResponse is Resource.Failure) apiLoginResponse.value = updateResponse
            }
            apiLoginResponse.value=response
        }
    }
    
    fun getHeaderEnrichment() {
        viewModelScope.launch {
            headerEnrichmentResponse.value = resultFromResponse { headerEnrichmentService.execute() }!!
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
        } catch (e: Exception) { }
    }
    
    fun sendLoginLogData() {
        viewModelScope.launch {
            try {
                sendLoginLogEvent.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun sendHeLogData(heLogData: HeaderEnrichmentLogData) {
        viewModelScope.launch {
            try {
                sendHeLogEvent.execute(heLogData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}