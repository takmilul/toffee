package com.banglalink.toffee.ui.category.music

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.banglalink.toffee.apiservice.*
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.exception.JobCanceledError
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.model.*
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StingrayViewModel @Inject constructor(
    private val getContentAssistedFactory: GetContents.AssistedFactory,
    private val getStingrayContentService: GetStingrayContentService
) : ViewModel() {
    val categoryId = SingleLiveEvent<Int>()
    val subCategoryId = SingleLiveEvent<Int>()
    val pageType = MutableLiveData<PageType>()
    val pageName = MutableLiveData<String>()
    val isDramaSeries = MutableLiveData<Boolean>()


    fun loadChannels(): Flow<PagingData<ChannelInfo>> {
        return channelRepo.getList()
    }

    private val channelRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getContentAssistedFactory.create(
                    ChannelRequestParams("", 0, "", 0, "LIVE")
                ), ApiNames.GET_CONTENTS_V5, pageName.value!!
            )
        })
    }

//////



        fun loadReportList(): Flow<PagingData<ChannelInfo>> {
            return reportRepo.getList()
        }
        private val reportRepo by lazy {
            BaseListRepositoryImpl({
                BaseNetworkPagingSource(
                    getStingrayContentService, ApiNames.GET_STINGRAY_CONTENTS,"Music"
                )
            })
        }

}