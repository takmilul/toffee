package com.banglalink.toffee.ui.recent

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.apiservice.GetHistory
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.database.entities.HistoryItem
import com.banglalink.toffee.data.repository.HistoryRepository
import com.banglalink.toffee.model.ChannelInfo

class RecentViewModel @ViewModelInject constructor(
    private val historyRepo: HistoryRepository
): BasePagingViewModel<HistoryItem>() {
    override val repo by lazy {
        BaseListRepositoryImpl({
            historyRepo.getAllItems()
        })
    }
}