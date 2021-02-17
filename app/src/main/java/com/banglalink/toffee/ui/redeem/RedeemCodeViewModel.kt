package com.banglalink.toffee.ui.redeem

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.RedeemReferralCodeBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.usecase.RedeemReferralCode
import com.banglalink.toffee.util.unsafeLazy

class RedeemCodeViewModel @ViewModelInject constructor(private val toffeeApi: ToffeeApi) : ViewModel() {

    private val redeemReferralCode by unsafeLazy {
        RedeemReferralCode(Preference.getInstance(), toffeeApi)
    }

    fun redeemReferralCode(referralCode: String):LiveData<Resource<RedeemReferralCodeBean>>{
        return resultLiveData {
            redeemReferralCode.execute(referralCode)
        }
    }
}