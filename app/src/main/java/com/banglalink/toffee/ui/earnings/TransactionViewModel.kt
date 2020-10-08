package com.banglalink.toffee.ui.earnings

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.apiservice.GetTransaction
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.Transaction

class TransactionViewModel @ViewModelInject constructor(override val apiService: GetTransaction): BasePagingViewModel<Transaction>()