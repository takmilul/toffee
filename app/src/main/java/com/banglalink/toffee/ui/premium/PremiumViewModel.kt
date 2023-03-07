package com.banglalink.toffee.ui.premium

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.*
import com.banglalink.toffee.data.network.request.CreatePaymentRequest
import com.banglalink.toffee.data.network.request.ExecutePaymentRequest
import com.banglalink.toffee.data.network.request.QueryPaymentRequest
import com.banglalink.toffee.data.network.response.*
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.model.ActivePack
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
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
    
    var selectedPack = savedState.getLiveData<PremiumPack>("selectedPack")
    var paymentMethod = savedState.getLiveData<PackPaymentMethodBean>("paymentMethod")
    
//    var selectedPaymentMethod = MutableLiveData<PackPaymentMethod>()

    var selectedPaymentMethod = MutableLiveData<PackPaymentMethod>()
    var packPurchaseResponseCode = SingleLiveEvent< Resource<PremiumPackStatusResponse.PremiumPackStatusBean>>()
    
    private var _bKashGrandTokenState = MutableSharedFlow<Resource<GrantTokenResponse>>()
    val bKashGrandTokenState = _bKashGrandTokenState.asSharedFlow()

    private var _bKashCreatePaymentState = MutableSharedFlow<Resource<CreatePaymentResponse>>()
    val bKashCreatePaymentState = _bKashCreatePaymentState.asSharedFlow()

    private var _bKashExecutePaymentState = MutableSharedFlow<Resource<ExecutePaymentResponse>>()
    val bKashExecutePaymentState = _bKashExecutePaymentState.asSharedFlow()

    private var _bKashStatusPaymentState = MutableSharedFlow<Resource<QueryPaymentResponse>>()
    val bKashStatusPaymentState = _bKashStatusPaymentState.asSharedFlow()

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
                    packId = selectedPack.value?.id,
                    packTitle = selectedPack.value?.packTitle,
                    contentList = selectedPack.value?.contentId,
                    paymentMethodId = this@PremiumViewModel.selectedPaymentMethod.value?.paymentMethodId,
                    packCode = this@PremiumViewModel.selectedPaymentMethod.value?.packCode,
                    packDetails = this@PremiumViewModel.selectedPaymentMethod.value?.packDetails,
                    packPrice = this@PremiumViewModel.selectedPaymentMethod.value?.packPrice,
                    packDuration = this@PremiumViewModel.selectedPaymentMethod.value?.packDuration
                )
            }
            packPurchaseResponseCode.value=response
        }
    }

    fun bkashGrandToken() {
        viewModelScope.launch {
            val response = resultFromResponse { bKashGrandTokenService.execute() }
            _bKashGrandTokenState.emit(response)
        }
    }
    
    fun bkashCreatePayment(token: String, requestBody: CreatePaymentRequest) {
        viewModelScope.launch {
            val response = resultFromResponse { bKashCreatePaymentService.execute(token, requestBody) }
            _bKashCreatePaymentState.emit(response)
        }
    }

    fun bkashExecutePayment(token: String, requestBody: ExecutePaymentRequest) {
        viewModelScope.launch {
            val response = resultFromResponse { bKashExecutePaymentService.execute(token, requestBody) }
            _bKashExecutePaymentState.emit(response)
        }
    }

    fun bkashStatusPayment(token: String, requestBody: QueryPaymentRequest) {
        viewModelScope.launch {
            val response = resultFromResponse { bKashStatusPaymentService.execute(token, requestBody) }
            _bKashStatusPaymentState.emit(response)
        }
    }
}