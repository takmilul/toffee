package com.banglalink.toffee.ui.earnings

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.Transaction

class TransactionAdapter(callback: BaseListItemCallback<Transaction>?): BasePagingDataAdapter<Transaction>(callback, ItemComparator()) {
    
    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_transaction
    }
}