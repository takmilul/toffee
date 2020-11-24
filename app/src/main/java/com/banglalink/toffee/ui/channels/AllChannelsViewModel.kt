package com.banglalink.toffee.ui.channels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.GetChannelWithCategory
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AllChannelsViewModel @ViewModelInject constructor(
    private val allChannelService: GetChannelWithCategory,
    private val tvChannelsRepo: TVChannelRepository
): BaseViewModel() {
    val selectedChannel = MutableLiveData<ChannelInfo?>()

    val repo by lazy {
        BaseListRepositoryImpl(
            {
                tvChannelsRepo.getAllChannels()
            }
        )
    }

    operator fun invoke(subcategoryId: Int): Flow<List<TVChannelItem>> {
        viewModelScope.launch {
            allChannelService(subcategoryId)
        }
        return tvChannelsRepo.getAllItems()
    }

    fun loadAllChannels(): Flow<PagingData<TVChannelItem>> {
        return repo.getList()
    }
}
