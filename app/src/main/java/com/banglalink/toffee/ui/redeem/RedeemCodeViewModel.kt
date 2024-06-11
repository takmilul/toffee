package com.banglalink.toffee.ui.redeem

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.apiservice.RedeemReferralCode
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.RedeemReferralCodeBean
import com.banglalink.toffee.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RedeemCodeViewModel @Inject constructor(
    private val redeemReferralCode: RedeemReferralCode,
) : ViewModel() {

    fun redeemReferralCode(referralCode: String): LiveData<Resource<RedeemReferralCodeBean?>> {
        return resultLiveData {
            redeemReferralCode.execute(referralCode)
        }
    }
}