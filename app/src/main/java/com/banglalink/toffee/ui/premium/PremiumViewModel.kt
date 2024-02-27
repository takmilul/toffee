package com.banglalink.toffee.ui.premium

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.AddTokenizedAccountInitService
import com.banglalink.toffee.apiservice.DataPackPurchaseService
import com.banglalink.toffee.apiservice.MnpStatusService
import com.banglalink.toffee.apiservice.PackPaymentMethodService
import com.banglalink.toffee.apiservice.PremiumPackDetailService
import com.banglalink.toffee.apiservice.PremiumPackListService
import com.banglalink.toffee.apiservice.PremiumPackStatusService
import com.banglalink.toffee.apiservice.PremiumPackSubHistoryService
import com.banglalink.toffee.apiservice.RechargeByBkashService
import com.banglalink.toffee.apiservice.RemoveTokenizeAccountApiService
import com.banglalink.toffee.apiservice.SubscriberPaymentInitService
import com.banglalink.toffee.apiservice.TokenizedAccountInfoApiService
import com.banglalink.toffee.apiservice.TokenizedPaymentMethodApiService
import com.banglalink.toffee.apiservice.VoucherService
import com.banglalink.toffee.data.network.request.AddTokenizedAccountInitRequest
import com.banglalink.toffee.data.network.request.DataPackPurchaseRequest
import com.banglalink.toffee.data.network.request.RechargeByBkashRequest
import com.banglalink.toffee.data.network.request.RemoveTokenizedAccountApiRequest
import com.banglalink.toffee.data.network.request.SubscriberPaymentInitRequest
import com.banglalink.toffee.data.network.request.TokenizedAccountInfoApiRequest
import com.banglalink.toffee.data.network.request.TokenizedPaymentMethodsApiRequest
import com.banglalink.toffee.data.network.response.MnpStatusBean
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.data.network.response.PackPaymentMethodBean
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.data.network.response.PremiumPackDetailBean
import com.banglalink.toffee.data.network.response.PremiumPackStatusBean
import com.banglalink.toffee.data.network.response.RechargeByBkashBean
import com.banglalink.toffee.data.network.response.RemoveTokenizeAccountApiResponse
import com.banglalink.toffee.data.network.response.SubHistoryResponseBean
import com.banglalink.toffee.data.network.response.SubscriberPaymentInitBean
import com.banglalink.toffee.data.network.response.TokenizedAccountInfo
import com.banglalink.toffee.data.network.response.TokenizedAccountInfoApiResponse
import com.banglalink.toffee.data.network.response.TokenizedPaymentMethodsApiResponse
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ActivePack
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.ClickableAdInventories
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.VoucherPaymentBean
import com.banglalink.toffee.usecase.PaymentLogFromDeviceData
import com.banglalink.toffee.usecase.SendPaymentLogFromDeviceEvent
import com.banglalink.toffee.util.SingleLiveEvent
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val mPref: SessionPreference,
    private val savedState: SavedStateHandle,
    private val premiumPackListService: PremiumPackListService,
    private val premiumPackDetailService: PremiumPackDetailService,
    private val packPaymentMethodService: PackPaymentMethodService,
    private val dataPackPurchaseService: DataPackPurchaseService,
    private val premiumPackStatusService: PremiumPackStatusService,
    private val rechargeByBkashService: RechargeByBkashService,
    private val subscriberPaymentInitService: SubscriberPaymentInitService,
    private val addTokenizedAccountInitService: AddTokenizedAccountInitService,
    private val sendPaymentLogFromDeviceEvent: SendPaymentLogFromDeviceEvent,
    private val premiumPackSubHistoryService: PremiumPackSubHistoryService,
    private val voucherService: VoucherService,
    private val mnpStatusService: MnpStatusService,
    private val tokenizedPaymentMethodApiService: TokenizedPaymentMethodApiService,
    private val tokenizedAccountInfoApiService: TokenizedAccountInfoApiService,
    private val removeTokenizeAccountApiService: RemoveTokenizeAccountApiService,
) : ViewModel() {
    
    private var _packListState = MutableSharedFlow<Resource<List<PremiumPack>>>()
    val packListState = _packListState.asSharedFlow()

//    private val _packListState: MutableLiveData<Resource<List<PremiumPack>>> = savedState.getLiveData("packListState")
//    val packListState: MutableLiveData<Resource<List<PremiumPack>>> get() = _packListState
    
    val packListScrollState = savedState.getLiveData<Int>("packListScrollState")
    
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
    
    val activePackListLiveData = SingleLiveEvent<Resource<List<ActivePack>>>()
    val activePackListAfterSubscriberPaymentLiveData = SingleLiveEvent<Resource<List<ActivePack>>>()
    
    private val _activePackListForDataPackOptionsLiveData = MutableSharedFlow<Resource<List<ActivePack>>>()
    val activePackListForDataPackOptionsLiveData = _activePackListForDataPackOptionsLiveData.asSharedFlow()
    
    private var _voucherPayment = MutableSharedFlow<Resource<VoucherPaymentBean?>>()
    val voucherPaymentState = _voucherPayment.asSharedFlow()
    
    var selectedPremiumPack = savedState.getLiveData<PremiumPack>("selectedPremiumPack")
    var paymentMethod = savedState.getLiveData<PackPaymentMethodBean>("paymentMethod")
    
    var selectedDataPackOption = savedState.getLiveData<PackPaymentMethod>("selectedDataPackOption")
    
    var packPurchaseResponseCodeTrialPack = SingleLiveEvent< Resource<PremiumPackStatusBean>>()
    var packPurchaseResponseCodeBlDataPackOptions = SingleLiveEvent< Resource<PremiumPackStatusBean>>()
    var packPurchaseResponseCodeBlDataPackOptionsWeb = SingleLiveEvent< Resource<PremiumPackStatusBean>>()
    var packPurchaseResponseVoucher = SingleLiveEvent< Resource<PremiumPackStatusBean>>()
    
    val rechargeByBkashUrlLiveData = SingleLiveEvent<Resource<RechargeByBkashBean?>>()
    val subscriberPaymentInitLiveData = SingleLiveEvent<Resource<SubscriberPaymentInitBean?>>()
    val addTokenizedAccountInitLiveData = SingleLiveEvent<Resource<SubscriberPaymentInitBean?>>()
    val premiumPackSubHistoryLiveData = SingleLiveEvent<Resource<SubHistoryResponseBean?>>()
    val clickedOnSubHistory = MutableLiveData<Boolean>()
    val clickedOnPackList = MutableLiveData<Boolean>()
    val mnpStatusLiveData = SingleLiveEvent<Resource<MnpStatusBean?>>()
    val mnpStatusLiveDataForPaymentDetail = SingleLiveEvent<Resource<MnpStatusBean?>>()

    var clickableAdInventories = savedState.getLiveData<ClickableAdInventories>("clickableAdInventories")
    var isLoggedInFromPaymentOptions = MutableLiveData<Boolean>()

    val tokenizedPaymentMethodsResponseCompose = MutableLiveData<TokenizedPaymentMethodsApiResponse?>()
    val isTokenizedPaymentMethodApiRespond = MutableLiveData<Boolean?>(null)
    val isTokenizedAccountInitFailed = MutableLiveData<Boolean?>(null)
    val tokenizedAccountInfoResponse = SingleLiveEvent<Resource<List<TokenizedAccountInfo>?>>()
    val removeTokenizeAccountResponse = MutableLiveData<RemoveTokenizeAccountApiResponse?>()

    fun getPremiumPackList(contentId: String = "0") {
        viewModelScope.launch {
            val response = resultFromResponse { premiumPackListService.loadData(contentId) }
            _packListState.emit(response)
//            packListState.value = response
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
    
    fun getPackStatus(contentId: Int = 0, packId: Int = 0) {
        viewModelScope.launch {
            val response = resultFromResponse { premiumPackStatusService.loadData(contentId, packId) }
            activePackListLiveData.value = response
        }
    }
    
    fun getPackStatusForDataPackOptions(contentId: Int = 0, packId: Int = 0) {
        viewModelScope.launch {
            val response = resultFromResponse { premiumPackStatusService.loadData(contentId, packId) }
            _activePackListForDataPackOptionsLiveData.emit(response)
        }
    }
    
    fun getPackStatusAfterSubscriberPayment(contentId: Int = 0, packId: Int = 0) {
        viewModelScope.launch {
            val response = resultFromResponse { premiumPackStatusService.loadData(contentId, packId) }
            activePackListAfterSubscriberPaymentLiveData.value = response
        }
    }
    
    fun purchaseDataPackTrialPack(dataPackPurchaseRequest: DataPackPurchaseRequest) {
        viewModelScope.launch {
            val response = resultFromResponse {
                dataPackPurchaseService.loadData(dataPackPurchaseRequest)
            }
            packPurchaseResponseCodeTrialPack.value = response
        }
    }
    
    fun purchaseDataPackBlDataPackOptions(dataPackPurchaseRequest: DataPackPurchaseRequest) {
        viewModelScope.launch {
            val response = resultFromResponse {
                dataPackPurchaseService.loadData(dataPackPurchaseRequest)
            }
            packPurchaseResponseCodeBlDataPackOptions.value = response
        }
    }
    
    fun purchaseDataPackBlDataPackOptionsWeb(dataPackPurchaseRequest: DataPackPurchaseRequest) {
        viewModelScope.launch {
            val response = resultFromResponse {
                dataPackPurchaseService.loadData(dataPackPurchaseRequest)
            }
            packPurchaseResponseCodeBlDataPackOptionsWeb.value = response
        }
    }
    
    fun purchaseDataPackVoucher(dataPackPurchaseRequest: DataPackPurchaseRequest){
        viewModelScope.launch {
            val response= resultFromResponse {
               dataPackPurchaseService.loadData(dataPackPurchaseRequest)
            }
            packPurchaseResponseVoucher.value = response
        }
    }
    
    fun getRechargeByBkashUrl(rechargeByBkashRequest: RechargeByBkashRequest) {
        viewModelScope.launch {
            val response = resultFromResponse { rechargeByBkashService.execute(rechargeByBkashRequest) }
            rechargeByBkashUrlLiveData.value = response
        }
    }
    
    fun getSubscriberPaymentInit(paymentType: String, subscriberPaymentInitRequest: SubscriberPaymentInitRequest) {
        viewModelScope.launch {
            val response = resultFromResponse { subscriberPaymentInitService.execute(paymentType, subscriberPaymentInitRequest) }
            subscriberPaymentInitLiveData.value = response
        }
    }

    fun getAddTokenizedAccountInit(paymentType: String, addTokenizedAccountInitRequest: AddTokenizedAccountInitRequest) {
        viewModelScope.launch {
            val response = resultFromResponse { addTokenizedAccountInitService.execute(paymentType, addTokenizedAccountInitRequest) }
            addTokenizedAccountInitLiveData.value = response
        }
    }
    
    fun sendPaymentLogFromDeviceData(paymentLogFromDeviceData: PaymentLogFromDeviceData) {
        viewModelScope.launch {
            try {
                sendPaymentLogFromDeviceEvent.execute(paymentLogFromDeviceData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun getPremiumPackSubscriptionHistory(){
        viewModelScope.launch {
            val response = resultFromResponse { premiumPackSubHistoryService.execute() }
            premiumPackSubHistoryLiveData.value = response
        }
    }
    
    fun voucherValidate(packId: Int,voucherCode:String,packName:String) {
        viewModelScope.launch {
            val response = resultFromResponse { voucherService.loadData(packId,voucherCode,packName) }
            _voucherPayment.emit(response)
        }
    }
    
    fun getMnpStatus() {
        viewModelScope.launch {
            val response = resultFromResponse { mnpStatusService.execute() }
            mnpStatusLiveData.value = response
        }
    }
    
    fun getMnpStatusForPaymentDetail() {
        viewModelScope.launch {
            val response = resultFromResponse { mnpStatusService.execute() }
            mnpStatusLiveDataForPaymentDetail.value = response
        }
    }

    fun getTokenizedPaymentMethods(body: TokenizedPaymentMethodsApiRequest){
        viewModelScope.launch {
            val response = resultFromResponse { tokenizedPaymentMethodApiService.execute(body) }
            when (response){
                is Resource.Success->{
                    isTokenizedPaymentMethodApiRespond.value = true
                    tokenizedPaymentMethodsResponseCompose.value = response.data
                }
                is Resource.Failure ->{
                    isTokenizedPaymentMethodApiRespond.value = false
                    appContext.showToast(response.error.msg)
                }
            }
        }
    }

    fun getTokenizedAccountInfo(paymentMethodId: Int, body: TokenizedAccountInfoApiRequest){
        viewModelScope.launch {
            val response = resultFromResponse { tokenizedAccountInfoApiService.execute(paymentMethodId, body) }
            tokenizedAccountInfoResponse.value = response
        }
    }

    fun removeTokenizeAccount(
        paymentMethodId: Int,
        body: RemoveTokenizedAccountApiRequest,
        onSuccess: ()->Unit? = {},
        onFailure: ()->Unit? = {},
    ){
        val gson = Gson()
        viewModelScope.launch {
            val response = resultFromResponse { removeTokenizeAccountApiService.execute(paymentMethodId, body) }
            when (response){
                is Resource.Success->{
                    removeTokenizeAccountResponse.value = response.data
                    sendPaymentLogFromDeviceData(
                        PaymentLogFromDeviceData(
                            id = System.currentTimeMillis() + mPref.customerId,
                            callingApiName = "removeTokenizedAccountFromAndroid",
                            cusWalletNo = body.walletNumber,
                            paymentCusId = body.paymentCusId,
                            rawResponse = gson.toJson(response)
                        )
                    )
                    onSuccess.invoke()
                }
                is Resource.Failure ->{
                    onFailure.invoke()
                    sendPaymentLogFromDeviceData(
                        PaymentLogFromDeviceData(
                            id = System.currentTimeMillis() + mPref.customerId,
                            callingApiName = "removeTokenizedAccountFromAndroid",
                            cusWalletNo = body.walletNumber,
                            paymentCusId = body.paymentCusId,
                            rawResponse = gson.toJson(response)
                        )
                    )
                    appContext.showToast(response.error.msg)
                }
            }
        }
    }
}