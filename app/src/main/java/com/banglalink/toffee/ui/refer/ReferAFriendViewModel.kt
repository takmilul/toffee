package com.banglalink.toffee.ui.refer

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.GetMyReferralCode
import com.banglalink.toffee.usecase.GetReferrerPolicy
import com.banglalink.toffee.util.unsafeLazy

class ReferAFriendViewModel @ViewModelInject constructor(private val toffeeApi: ToffeeApi) : BaseViewModel() {

    private val getMyReferralCode by unsafeLazy {
        GetMyReferralCode(Preference.getInstance(), toffeeApi)
    }

    private val getReferralPolicy by unsafeLazy {
        GetReferrerPolicy(Preference.getInstance(), toffeeApi)
    }

    fun getMyReferralCode():LiveData<Resource<ReferralForm>>{
        return resultLiveData {
            val refPolicy = getReferralPolicy.execute()
            val refCode = getMyReferralCode.execute()
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