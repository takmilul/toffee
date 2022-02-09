package com.banglalink.toffee.ui.category.music.stingray

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.GetStingrayContentService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.ChannelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class StingrayViewModel @Inject constructor(
    private val getStingrayContentService: GetStingrayContentService,
) : ViewModel() {
    
    fun loadStingrayList(): Flow<PagingData<ChannelInfo>> {
        return stingrayRepo.getList()
    }
    
    private val stingrayRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getStingrayContentService, ApiNames.GET_STINGRAY_CONTENTS, "Music"
            )
        })
    }
}