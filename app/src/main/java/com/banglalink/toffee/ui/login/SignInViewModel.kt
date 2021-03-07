package com.banglalink.toffee.ui.login

import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import androidx.lifecycle.LiveData
import com.banglalink.toffee.apiservice.CheckReferralCodeStatus
import com.banglalink.toffee.apiservice.SignInByPhone
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val pref: Preference,
    private val signInByPhone: SignInByPhone,
    private val checkReferralCodeStatus: CheckReferralCodeStatus,
) : BaseViewModel() {

    fun signIn(phoneNumber: String, referralCode: String): LiveData<Resource<String>> {
        return resultLiveData {
            if (referralCode.isNotBlank()) {
                checkReferralCodeStatus.execute(phoneNumber, referralCode)
            }
            signInByPhone.execute(phoneNumber, referralCode)
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