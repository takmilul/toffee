package com.banglalink.toffee.ui.points

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.apiservice.GetRedeemPoints
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.RedeemPointsBean
import com.banglalink.toffee.model.RedeemPointsMsg
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.unsafeLazy

class RedeemPointsViewModel: ViewModel() {
    
    private val aboutPoints by unsafeLazy { GetRedeemPoints() }
    
    fun setRedeemPoints(): LiveData<Resource<RedeemPointsBean>> {
        return resultLiveData {
            aboutPoints.execute()
        }
    }
    
    fun redeemPoints(): LiveData<Resource<RedeemPointsMsg>>{
        return resultLiveData {
            aboutPoints.redeem()
        }
    }
}