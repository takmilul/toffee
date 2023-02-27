package com.banglalink.toffee.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.PackPaymentMethodService
import com.banglalink.toffee.apiservice.PremiumPackDetailService
import com.banglalink.toffee.apiservice.PremiumPackListService
import com.banglalink.toffee.data.network.response.PackPaymentMethodBean
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.data.network.response.PremiumPackDetailBean
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumPackListService: PremiumPackListService,
    private val premiumPackDetailService: PremiumPackDetailService,
    private val packPaymentMethodService: PackPaymentMethodService
) : ViewModel() {
    
    private var _premiumPackListState = MutableSharedFlow<Resource<List<PremiumPack>>>()
    val premiumPackListState = _premiumPackListState.asSharedFlow()
    
    private var _premiumPackDetailState = MutableSharedFlow<Resource<PremiumPackDetailBean?>>()
    val premiumPackDetailState = _premiumPackDetailState.asSharedFlow()
    
    private val _premiumPackLinearContentListMutableState = MutableSharedFlow<List<ChannelInfo>?>()
    val premiumPackLinearContentListState = _premiumPackLinearContentListMutableState.asSharedFlow()
    
    private var _premiumPackVodContentListMutableState = MutableSharedFlow<List<ChannelInfo>?>()
    var premiumPackVodContentListState = _premiumPackVodContentListMutableState.asSharedFlow()

    private var _paymentMethodState = MutableSharedFlow<Resource<PackPaymentMethodBean>>()
    val paymentMethodState = _paymentMethodState.asSharedFlow()
    
    fun getPremiumPackList(contentId: String = "0") {
        viewModelScope.launch {
            val response = resultFromResponse { premiumPackListService.loadData(contentId) }
            _premiumPackListState.emit(response)
        }
    }
    
    fun getPremiumPackDetail(packId: Int) {
        viewModelScope.launch {
            val response = resultFromResponse { premiumPackDetailService.loadData(packId) }
            _premiumPackDetailState.emit(response)
        }
    }
    
    fun setLinearContentState(linearContentList: List<ChannelInfo>) {
        viewModelScope.launch {
            _premiumPackLinearContentListMutableState.emit(linearContentList)
        }
    }
    
    fun setVodContentState(vodContentList: List<ChannelInfo>) {
        viewModelScope.launch {
            _premiumPackVodContentListMutableState.emit(vodContentList)
        }
    }
    
    fun getPackPaymentMethodList(packageId: Int){
        viewModelScope.launch {
            val response = resultFromResponse { packPaymentMethodService.loadData(packageId) }
            _paymentMethodState.emit(response)
        }
    }
}