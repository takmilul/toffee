package com.banglalink.toffee.ui.login

import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.CheckReferralCodeStatus
import com.banglalink.toffee.apiservice.LoginByPhone
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val pref: SessionPreference,
    private val loginByPhone: LoginByPhone,
    private val checkReferralCodeStatus: CheckReferralCodeStatus,
) : BaseViewModel() {

    val loginByPhoneResponse = SingleLiveEvent<Resource<Any>>()

    fun login(phoneNumber: String, referralCode: String) {
        viewModelScope.launch {
            if (referralCode.isNotBlank()) {
               val referResponse= resultFromResponse { checkReferralCodeStatus.execute(phoneNumber, referralCode)}
                if(referResponse is Resource.Failure)
                {
                    loginByPhoneResponse.value = referResponse
                    return@launch
                }
            }
           val response= resultFromResponse {  loginByPhone.execute(phoneNumber, referralCode) }
            loginByPhoneResponse.value=response
        }
    }

    fun deletePreviousDatabase() {
        try {
            val data: File = Environment.getDataDirectory()
            val previousDBPath = "/data/com.banglalink.toffee/databases/" + "toffee_database"
            val previousDB = File(data, previousDBPath)
            if (previousDB.exists()) {
                pref.isPreviousDbDeleted = SQLiteDatabase.deleteDatabase(previousDB)
            } else {
                pref.isPreviousDbDeleted = true
            }
        } catch (e: Exception) {
        }
    }
}