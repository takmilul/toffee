package com.banglalink.toffee.ui.login

import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.CheckReferralCodeStatus
import com.banglalink.toffee.usecase.SigninByPhone
import com.banglalink.toffee.util.unsafeLazy
import java.io.File

class SignInViewModel @ViewModelInject constructor(private val pref:Preference, private val toffeeApi: ToffeeApi): BaseViewModel() {
    
    private val signingByPhone by unsafeLazy {
        SigninByPhone(Preference.getInstance(), toffeeApi)
    }

    private val checkReferralCodeStatus by unsafeLazy {
        CheckReferralCodeStatus(toffeeApi)
    }

    fun signIn(phoneNumber: String, referralCode: String):LiveData<Resource<String>> {
        return resultLiveData{
            if(referralCode.isNotBlank()){
                checkReferralCodeStatus.execute(phoneNumber,referralCode)
            }
            signingByPhone.execute(phoneNumber,referralCode)
        }
    }

    fun deletePreviousDatabase(){
        try {
                val data: File = Environment.getDataDirectory()
                val previousDBPath = "/data/com.banglalink.toffee/databases/" + "toffee_database"
                val previousDB = File(data, previousDBPath)
                if (previousDB.exists()) {
                    pref.isPreviousDbDeleted = SQLiteDatabase.deleteDatabase(previousDB)
                } else {
                    pref.isPreviousDbDeleted = true
                }
        }
        catch ( e:Exception) {
        }
    }
}