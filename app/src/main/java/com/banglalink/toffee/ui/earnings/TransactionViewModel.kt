package com.banglalink.toffee.ui.earnings

import com.banglalink.toffee.model.Transaction
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel
import com.banglalink.toffee.usecase.GetTransaction

class TransactionViewModel: SingleListViewModel<Transaction>() {
    override var repo: SingleListRepository<Transaction> = GetTransaction()
}