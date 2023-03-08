package com.banglalink.toffee.ui.premium

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.BkashCreatePaymentService
import com.banglalink.toffee.apiservice.BkashExecutePaymentService
import com.banglalink.toffee.apiservice.BkashGrandTokenService
import com.banglalink.toffee.apiservice.BkashStatusPaymentService
import com.banglalink.toffee.apiservice.DataPackPurchaseService
import com.banglalink.toffee.apiservice.PackPaymentMethodService
import com.banglalink.toffee.apiservice.PremiumPackDetailService
import com.banglalink.toffee.apiservice.PremiumPackListService
import com.banglalink.toffee.apiservice.PremiumPackStatusService
import com.banglalink.toffee.data.network.request.CreatePaymentRequest
import com.banglalink.toffee.data.network.request.ExecutePaymentRequest
import com.banglalink.toffee.data.network.request.QueryPaymentRequest
import com.banglalink.toffee.data.network.response.CreatePaymentResponse
import com.banglalink.toffee.data.network.response.ExecutePaymentResponse
import com.banglalink.toffee.data.network.response.GrantTokenResponse
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.data.network.response.PackPaymentMethodBean
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.data.network.response.PremiumPackDetailBean
import com.banglalink.toffee.data.network.response.PremiumPackStatusResponse
import com.banglalink.toffee.data.network.response.QueryPaymentResponse
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.model.ActivePack
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

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
    private val bKashStatusPaymentService: BkashStatusPaymentService,
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
    var packPurchaseResponseCode = SingleLiveEvent< Resource<PremiumPackStatusResponse.PremiumPackStatusBean>>()
    
    private var _bKashGrandTokenState = MutableSharedFlow<Resource<GrantTokenResponse>>()
    val bKashGrandTokenState = _bKashGrandTokenState.asSharedFlow()
    
    private var _bKashCreatePaymentState = MutableSharedFlow<Resource<CreatePaymentResponse>>()
    val bKashCreatePaymentState = _bKashCreatePaymentState.asSharedFlow()
    
    private var _bKashExecutePaymentState = MutableSharedFlow<Resource<ExecutePaymentResponse>>()
    val bKashExecutePaymentState = _bKashExecutePaymentState.asSharedFlow()
    
    private var _bKashStatusPaymentState = MutableSharedFlow<Resource<QueryPaymentResponse>>()
    val bKashQueryPaymentState = _bKashStatusPaymentState.asSharedFlow()
    
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
    
    fun purchaseDataPack() {
        viewModelScope.launch {
            val response = resultFromResponse {
                dataPackPurchaseService.loadData(
                    packId = selectedPremiumPack.value?.id,
                    packTitle = selectedPremiumPack.value?.packTitle,
                    contentList = selectedPremiumPack.value?.contentId,
                    paymentMethodId = this@PremiumViewModel.selectedDataPackOption.value?.paymentMethodId,
                    packCode = this@PremiumViewModel.selectedDataPackOption.value?.packCode,
                    packDetails = this@PremiumViewModel.selectedDataPackOption.value?.packDetails,
                    packPrice = this@PremiumViewModel.selectedDataPackOption.value?.packPrice,
                    packDuration = this@PremiumViewModel.selectedDataPackOption.value?.packDuration
                )
            }
            packPurchaseResponseCode.value=response
        }
    }
    
    fun bKashGrandToken() {
        viewModelScope.launch {
            val response = resultFromResponse { bKashGrandTokenService.execute() }
            _bKashGrandTokenState.emit(response)
        }
    }
    
    fun bKashCreatePayment(token: String, requestBody: CreatePaymentRequest) {
        viewModelScope.launch {
            val response = resultFromResponse { bKashCreatePaymentService.execute(token, requestBody) }
            _bKashCreatePaymentState.emit(response)
        }
    }
    
    fun bKashExecutePayment(token: String, requestBody: ExecutePaymentRequest) {
        viewModelScope.launch {
            val response = resultFromResponse { bKashExecutePaymentService.execute(token, requestBody) }
            _bKashExecutePaymentState.emit(response)
        }
    }
    
    fun bKashQueryPayment(token: String, requestBody: QueryPaymentRequest) {
        viewModelScope.launch {
            val response = resultFromResponse { bKashStatusPaymentService.execute(token, requestBody) }
            _bKashStatusPaymentState.emit(response)
        }
    }
}