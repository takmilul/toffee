package com.banglalink.toffee.ui.fmradio

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.GetFmRadioContentService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.ChannelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class FmViewModel @Inject constructor(
    private val getFmRadioContentService: GetFmRadioContentService
): ViewModel(){
    
    fun loadFmRadioList(): Flow<PagingData<ChannelInfo>> {
        return fmRadioRepo.getList()
    }

    private val fmRadioRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getFmRadioContentService, ApiNames.GET_FM_RADIO_CONTENTS, "Music"
            )
        })
    }
}