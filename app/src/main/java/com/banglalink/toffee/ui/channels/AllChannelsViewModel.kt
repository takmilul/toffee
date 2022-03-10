package com.banglalink.toffee.ui.channels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.GetChannelWithCategory
import com.banglalink.toffee.apiservice.GetStingrayContentService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.data.database.entities.TVChannelItem
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
) : ViewModel() {
    
    val selectedChannel = MutableLiveData<ChannelInfo?>()
    
    operator fun invoke(subcategoryId: Int, isStingray: Boolean = false): Flow<List<TVChannelItem>> {
        viewModelScope.launch {
            try {
                if (!isStingray) {
                    allChannelService(subcategoryId)
                } else {
                    getStingrayContentService.loadData(0, 100)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return if (isStingray) tvChannelsRepo.getStingrayItems() else tvChannelsRepo.getAllItems()
    }
    
    fun loadAllChannels(isStingray: Boolean): Flow<PagingData<TVChannelItem>> {
        return BaseListRepositoryImpl({
            tvChannelsRepo.getAllChannels(isStingray)
        }).getList()
    }
    
    fun loadRecentTvChannels(isStingray: Boolean = false): Flow<List<TVChannelItem>> {
        return if (isStingray) tvChannelsRepo.getStingrayRecentItems() else tvChannelsRepo.getRecentItems()
    }
}
