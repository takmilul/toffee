package com.banglalink.toffee.ui.recent

import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.database.entities.HistoryItem
import com.banglalink.toffee.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecentViewModel @Inject constructor(
    private val historyRepo: HistoryRepository,
) : BasePagingViewModel<HistoryItem>() {

    override val repo by lazy {
        BaseListRepositoryImpl({
            historyRepo.getAllItems()
        })
    }
}