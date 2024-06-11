package com.banglalink.toffee.ui.refer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.apiservice.GetMyReferralCodeService
import com.banglalink.toffee.apiservice.GetReferrerPolicyService
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReferAFriendViewModel @Inject constructor(
    private val myReferralCode: GetMyReferralCodeService,
    private val referrerPolicy: GetReferrerPolicyService,
) : ViewModel() {

    fun getMyReferralCode(): LiveData<Resource<ReferralForm?>> {
        return resultLiveData {
            val refPolicy = referrerPolicy.execute()
            val refCode = myReferralCode.execute()
            
            if (refCode == null || refPolicy == null) return@resultLiveData null
            
            ReferralForm(refCode.referralCode,
                refCode.sharableText,
                if (refPolicy.isPromotionMessageEnabled) refPolicy.promotionMessage ?: "" else "",
                if (refPolicy.messageReadMoreEnabled) refPolicy.readMoreDetails ?: "" else "",
                refPolicy.fontSize,
                refPolicy.fontColor
            )
        }
    }
}