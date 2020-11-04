package com.banglalink.toffee.ui.redeem

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.RedeemReferralCodeBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.usecase.RedeemReferralCode
import com.banglalink.toffee.util.unsafeLazy

class RedeemCodeViewModel : ViewModel() {

    private val redeemReferralCode by unsafeLazy {
        RedeemReferralCode(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun redeemReferralCode(referralCode: String):LiveData<Resource<RedeemReferralCodeBean>>{
        return resultLiveData {
            redeemReferralCode.execute(referralCode)
        }
    }
}