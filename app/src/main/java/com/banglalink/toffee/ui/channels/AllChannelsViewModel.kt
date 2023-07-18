package com.banglalink.toffee.ui.channels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.apiservice.GetChannelWithCategory
import com.banglalink.toffee.apiservice.GetContentService
import com.banglalink.toffee.apiservice.GetFmRadioContentService
import com.banglalink.toffee.apiservice.GetStingrayContentService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.model.ChannelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllChannelsViewModel @Inject constructor(
    private val tvChannelsRepo: TVChannelRepository,
    private val allChannelService: GetChannelWithCategory,
    private val getStingrayContentService: GetStingrayContentService,
    private val getFmRadioContentService: GetFmRadioContentService,
    private val getContentAssistedFactory: GetContentService.AssistedFactory,
) : ViewModel() {
    
    val selectedChannel = MutableLiveData<ChannelInfo?>()
    val isFromSportsCategory = MutableLiveData<Boolean>()
    
    fun getChannels(subcategoryId: Int, isStingray: Boolean = false, isFmRadio: Boolean): Flow<List<TVChannelItem>?> {
        viewModelScope.launch {
            try {
                if (isFmRadio && !isStingray) {
                    getFmRadioContentService.loadData(0,100)
                }
                else if(isStingray && !isFmRadio){
                    getStingrayContentService.loadData(0, 100)
                } else {
                    allChannelService.loadData(subcategoryId)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return if (isStingray) tvChannelsRepo.getStingrayItems() else if(isFmRadio) tvChannelsRepo.getFmItems() else tvChannelsRepo.getAllItems()
    }
    
    fun loadAllChannels(isStingray: Boolean, isFromSportsCategory: Boolean): Flow<PagingData<out Any>> {
        return BaseListRepositoryImpl({
            if (isFromSportsCategory) {
                BaseNetworkPagingSource(
                    getContentAssistedFactory.create(
                        ChannelRequestParams("Sports", 16, "", 0, "LIVE")
                    ), ApiNames.GET_CONTENTS_V5, BrowsingScreens.CATEGORY_SCREEN
                )
            } else {
                tvChannelsRepo.getAllChannels(isStingray)
            }
        }).getList(10)
    }
    
    fun loadRecentTvChannels(isStingray: Boolean = false): Flow<List<TVChannelItem>?> {
        return if (isStingray) tvChannelsRepo.getStingrayRecentItems() else tvChannelsRepo.getRecentItemsFlow()
    }
}