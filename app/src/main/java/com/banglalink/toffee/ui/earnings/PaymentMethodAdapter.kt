package com.banglalink.toffee.ui.earnings

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.PaymentMethod
import com.banglalink.toffee.ui.common.MyBaseAdapterV2

class PaymentMethodAdapter(callback: BaseListItemCallback<PaymentMethod>?): MyBaseAdapterV2<PaymentMethod>(callback) {
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_payment_method
    }
}