package com.banglalink.toffee.ui.premium

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.PremiumPackDetailService
import com.banglalink.toffee.apiservice.PremiumPackListService
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.data.network.response.PremiumPackDetailBean
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumPackListService: PremiumPackListService,
    private val premiumPackDetailService: PremiumPackDetailService
): ViewModel() {

    private var _premiumPremiumPackListLiveData = MutableLiveData<Resource<List<PremiumPack>>>()
    val premiumPackListLiveData = _premiumPremiumPackListLiveData.toLiveData()
    
    private var _premiumPremiumPackDetailLiveData = MutableLiveData<Resource<PremiumPackDetailBean?>>()
    val premiumPackDetailLiveData = _premiumPremiumPackDetailLiveData.toLiveData()
    
    val premiumPackLinearContentListLiveData = MutableLiveData<List<ChannelInfo>?>()
    var premiumPackVodContentListLiveData = MutableLiveData<List<ChannelInfo>?>()
    
    fun getPremiumPackList(contentId: String = "0") {
        viewModelScope.launch {
            val response = resultFromResponse { premiumPackListService.loadData(contentId) }
            _premiumPremiumPackListLiveData.postValue(response)
        }
    }
    
    fun getPremiumPackDetail(packId: Int = 0) {
        viewModelScope.launch {
            val response = resultFromResponse { premiumPackDetailService.loadData(packId) }
            _premiumPremiumPackDetailLiveData.postValue(response)
        }
    }
}