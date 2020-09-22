package com.banglalink.toffee.ui.earnings

import com.banglalink.toffee.R
import com.banglalink.toffee.model.PaymentMethod
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class PaymentMethodAdapter(callback: SingleListItemCallback<PaymentMethod>?): MyBaseAdapterV2<PaymentMethod>(callback) {
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_payment_method
    }
}