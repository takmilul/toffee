package com.banglalink.toffee.ui.explore

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.banglalink.toffee.apiservice.PartnersListService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PartnersViewModel @Inject constructor(
    private val partnersApiService: PartnersListService.AssistedFactory
): BaseViewModel() {
    
    val getPartnersList by lazy { 
        partnersListRepo.getList().cachedIn(viewModelScope)
    }
    
    private val partnersListRepo by lazy { 
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(partnersApiService.create(
                ChannelRequestParams("", 0, "", 0, "LIVE")
            ))
        })
    }
}