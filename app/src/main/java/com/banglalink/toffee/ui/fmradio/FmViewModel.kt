package com.banglalink.toffee.ui.fmradio

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.BkashCreatePaymentService
import com.banglalink.toffee.apiservice.BkashExecutePaymentService
import com.banglalink.toffee.apiservice.BkashGrandTokenService
import com.banglalink.toffee.apiservice.BkashQueryPaymentService
import com.banglalink.toffee.apiservice.DataPackPurchaseService
import com.banglalink.toffee.apiservice.GetFmRadioContentService
import com.banglalink.toffee.apiservice.GetStingrayContentService
import com.banglalink.toffee.apiservice.PackPaymentMethodService
import com.banglalink.toffee.apiservice.PremiumPackDetailService
import com.banglalink.toffee.apiservice.PremiumPackListService
import com.banglalink.toffee.apiservice.PremiumPackStatusService
import com.banglalink.toffee.apiservice.RechargeByBkashService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.SendPaymentLogFromDeviceEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FmViewModel @Inject constructor(
    private val getFmRadioContentService: GetFmRadioContentService,

    ): ViewModel(){


    fun loadStingrayList(): Flow<PagingData<ChannelInfo>> {
        return stingrayRepo.getList()
    }

    private val stingrayRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getFmRadioContentService, ApiNames.GET_FM_RADIO_CONTENTS, "Music"
            )
        })
    }
}