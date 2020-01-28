package com.banglalink.toffee.ui.redeem

import android.app.Application
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.RedeemReferralCodeBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.RedeemReferralCode
import com.banglalink.toffee.util.unsafeLazy

class RedeemCodeViewModel(@NonNull application: Application) : BaseViewModel(application) {

    private val redeemReferralCode by unsafeLazy {
        RedeemReferralCode(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun redeemReferralCode(referralCode: String):LiveData<Resource<RedeemReferralCodeBean>>{
        return resultLiveData {
            redeemReferralCode.execute(referralCode)
        }
    }
}