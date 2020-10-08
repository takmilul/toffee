package com.banglalink.toffee.ui.earnings

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.apiservice.GetTransaction
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Transaction

class TransactionViewModel @ViewModelInject constructor(val apiService: GetTransaction): BasePagingViewModel<Transaction>() {
    override val repo: BaseListRepository<Transaction> by lazy {
        BaseListRepositoryImpl(BaseNetworkPagingSource(apiService))
    }
}