package com.banglalink.toffee.ui.premium

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.*
import com.banglalink.toffee.data.network.request.*
import com.banglalink.toffee.data.network.response.*
import com.banglalink.toffee.data.network.util.resultFromExternalResponse
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.model.ActivePack
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val premiumPackListService: PremiumPackListService,
    private val premiumPackDetailService: PremiumPackDetailService,
    private val packPaymentMethodService: PackPaymentMethodService,
    private val dataPackPurchaseService: DataPackPurchaseService,
    private val premiumPackStatusService: PremiumPackStatusService,
    private val bKashGrandTokenService: BkashGrandTokenService,
    private val bKashCreatePaymentService: BkashCreatePaymentService,
    private val bKashExecutePaymentService: BkashExecutePaymentService,
    private val bKashQueryPaymentService: BkashQueryPaymentService,
    private val rechargeByBkashService: RechargeByBkashService,
) : ViewModel() {
    
    private var _packListState = MutableSharedFlow<Resource<List<PremiumPack>>>()
    val packListState = _packListState.asSharedFlow()
    
    private var _packDetailState = MutableSharedFlow<Resource<PremiumPackDetailBean?>>()
    val packDetailState = _packDetailState.asSharedFlow()
    
    private val _packChannelListMutableState = MutableSharedFlow<List<ChannelInfo>?>()
    val packChannelListState = _packChannelListMutableState.asSharedFlow()
    
    private var _packContentListMutableState = MutableSharedFlow<List<ChannelInfo>?>()
    var packContentListState = _packContentListMutableState.asSharedFlow()
    
//    private var _packContentListsMutableState = MutableStateFlow<List<ChannelInfo>?>(null)
//    var packContentListsState = _packContentListsMutableState.asSharedFlow()
    
    private var _paymentMethodState = MutableSharedFlow<Resource<PackPaymentMethodBean>>()
    val paymentMethodState = _paymentMethodState.asSharedFlow()
    
    private val _activePackListLiveData = MutableSharedFlow<Resource<List<ActivePack>>>()
    val activePackListLiveData = _activePackListLiveData.asSharedFlow()
    
    var selectedPremiumPack = savedState.getLiveData<PremiumPack>("selectedPremiumPack")
    var paymentMethod = savedState.getLiveData<PackPaymentMethodBean>("paymentMethod")
    
    var selectedDataPackOption = MutableLiveData<PackPaymentMethod>()
    var packPurchaseResponseCode = SingleLiveEvent< Resource<PremiumPackStatusBean>>()
    
    val bKashGrandTokenLiveData = SingleLiveEvent<Resource<GrantTokenResponse>>()
    val bKashCreatePaymentLiveData = SingleLiveEvent<Resource<CreatePaymentResponse>>()
    val bKashCreatePaymentLiveDataWebView = SingleLiveEvent<Resource<CreatePaymentResponse>>()
    val bKashExecutePaymentLiveData = SingleLiveEvent<Resource<ExecutePaymentResponse>>()
    val bKashQueryPaymentLiveData = SingleLiveEvent<Resource<QueryPaymentResponse>>()
    val rechargeByBkashUrlLiveData = SingleLiveEvent<Resource<RechargeByBkashBean?>>()
    
    fun getPremiumPackList(contentId: String = "0") {
        viewModelScope.launch {
            val response = resultFromResponse { premiumPackListService.loadData(contentId) }
            _packListState.emit(response)
        }
    }
    
    fun getPremiumPackDetail(packId: Int) {
        viewModelScope.launch {
            val response = resultFromResponse { premiumPackDetailService.loadData(packId) }
            _packDetailState.emit(response)
        }
    }
    
    fun setLinearContentState(linearContentList: List<ChannelInfo>) {
        viewModelScope.launch {
            _packChannelListMutableState.emit(linearContentList)
        }
    }
    
    fun setVodContentState(vodContentList: List<ChannelInfo>) {
        viewModelScope.launch {
//            _packContentListsMutableState.value = vodContentList
            _packContentListMutableState.emit(vodContentList)
        }
    }
    
    fun getPackPaymentMethodList(packageId: Int) {
        viewModelScope.launch {
            val response = resultFromResponse { packPaymentMethodService.loadData(packageId) }
            _paymentMethodState.emit(response)
        }
    }
    
    fun getPackStatus(contentId: Int = 0) {
        viewModelScope.launch {
            val response = resultFromResponse { premiumPackStatusService.loadData(contentId) }
            _activePackListLiveData.emit(response)
        }
    }
    
    fun purchaseDataPack(dataPackPurchaseRequest: DataPackPurchaseRequest) {
        viewModelScope.launch {
            val response = resultFromResponse {
                dataPackPurchaseService.loadData(dataPackPurchaseRequest)
            }
            packPurchaseResponseCode.value = response
        }
    }
    
    fun bKashGrandToken() {
        viewModelScope.launch {
            val response = resultFromExternalResponse { bKashGrandTokenService.execute() }
            bKashGrandTokenLiveData.value = response
        }
    }
    
    fun bKashCreatePayment(token: String, requestBody: CreatePaymentRequest) {
        viewModelScope.launch {
            val response = resultFromExternalResponse { bKashCreatePaymentService.execute(token, requestBody) }
            bKashCreatePaymentLiveData.value = response
        }
    }

    fun bKashCreatePaymentWebView(token: String, requestBody: CreatePaymentRequest) {
        viewModelScope.launch {
            val response = resultFromExternalResponse { bKashCreatePaymentService.execute(token, requestBody) }
            bKashCreatePaymentLiveDataWebView.value = response
        }
    }
    
    fun bKashExecutePayment(token: String, requestBody: ExecutePaymentRequest) {
        viewModelScope.launch {
            val response = resultFromExternalResponse { bKashExecutePaymentService.execute(token, requestBody) }
            bKashExecutePaymentLiveData.value = response
        }
    }
    
    fun bKashQueryPayment(token: String, requestBody: QueryPaymentRequest) {
        viewModelScope.launch {
            val response = resultFromExternalResponse { bKashQueryPaymentService.execute(token, requestBody) }
            bKashQueryPaymentLiveData.value = response
        }
    }
    
    fun getRechargeByBkashUrl(rechargeByBkashRequest: RechargeByBkashRequest) {
        viewModelScope.launch {
            val response = resultFromResponse { rechargeByBkashService.execute(rechargeByBkashRequest) }
            rechargeByBkashUrlLiveData.value = response
        }
    }
}