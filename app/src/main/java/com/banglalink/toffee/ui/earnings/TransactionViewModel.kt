package com.banglalink.toffee.ui.earnings

import com.banglalink.toffee.apiservice.GetTransaction
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(val apiService: GetTransaction): BasePagingViewModel<Transaction>() {
    override val repo: BaseListRepository<Transaction> by lazy {
        BaseListRepositoryImpl({ BaseNetworkPagingSource(apiService) })
    }
}