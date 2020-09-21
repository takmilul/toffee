package com.banglalink.toffee.ui.earnings

import com.banglalink.toffee.R
import com.banglalink.toffee.model.Transaction
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class TransactionAdapter(callback: SingleListItemCallback<Transaction>?): MyBaseAdapterV2<Transaction>(callback) {
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_transaction
    }
}