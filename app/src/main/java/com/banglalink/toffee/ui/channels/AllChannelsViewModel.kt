package com.banglalink.toffee.ui.channels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.GetChannelWithCategory
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
    private val allChannelService: GetChannelWithCategory,
    private val tvChannelsRepo: TVChannelRepository
): ViewModel() {
    val selectedChannel = MutableLiveData<ChannelInfo?>()

    val repo by lazy {
        BaseListRepositoryImpl({
                tvChannelsRepo.getAllChannels()
            }
        )
    }

    operator fun invoke(subcategoryId: Int, isStingray: Boolean = false): Flow<List<TVChannelItem>> {
        viewModelScope.launch {
            if (!isStingray) {
                try {
                    allChannelService(subcategoryId)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
        return if(isStingray) tvChannelsRepo.getStingrayItems() else tvChannelsRepo.getAllItems()
    }

    fun loadAllChannels(): Flow<PagingData<TVChannelItem>> {
        return repo.getList()
    }

    fun loadRecentTvChannels(isStingray: Boolean = false): Flow<List<TVChannelItem>> {
        return if (isStingray) tvChannelsRepo.getStingrayRecentItems() else tvChannelsRepo.getRecentItems()
    }
}
