package com.banglalink.toffee.ui.refer

import android.app.Application
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ReferralCodeBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.GetMyReferralCode
import com.banglalink.toffee.util.unsafeLazy

class ReferAFriendViewModel(@NonNull application: Application) : BaseViewModel(application) {

    private val getMyReferralCode by unsafeLazy {
        GetMyReferralCode(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun getMyReferralCode():LiveData<Resource<ReferralCodeBean>>{
        return resultLiveData {
            getMyReferralCode.execute()
        }
    }
}