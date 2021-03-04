package com.banglalink.toffee.ui.refer

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import com.banglalink.toffee.apiservice.GetMyReferralCode
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.apiservice.GetReferrerPolicy

class ReferAFriendViewModel @ViewModelInject constructor(
    private val myReferralCode: GetMyReferralCode,
    private val referrerPolicy: GetReferrerPolicy
) : BaseViewModel() {

    fun getMyReferralCode():LiveData<Resource<ReferralForm>>{
        return resultLiveData {
            val refPolicy = referrerPolicy.execute()
            val refCode = myReferralCode.execute()
            ReferralForm(refCode.referralCode,
                refCode.sharableText,
                if(refPolicy.isPromotionMessageEnabled) refPolicy.promotionMessage?:"" else "",
                if(refPolicy.messageReadMoreEnabled) refPolicy.readMoreDetails?:"" else "",
                refPolicy.fontSize,
                refPolicy.fontColor
            )
        }
    }
}