package com.banglalink.toffee.apiservice

import com.banglalink.toffee.model.PaymentMethod
import com.banglalink.toffee.ui.common.SingleListRepository

class GetPaymentMethod : SingleListRepository<PaymentMethod> {
    var mOffset: Int = 0
        private set
    private val limit = 10

    override suspend fun execute(): List<PaymentMethod> {
        if (mOffset < limit) {
            mOffset += limit

            val paymentMethodList: MutableList<PaymentMethod> = mutableListOf()
            paymentMethodList.add(PaymentMethod("Bank", "Brac Bank Limited", "Md. Mahmudul Hossain", "**** - **** - **** - 2569", null, null, null))
            paymentMethodList.add(PaymentMethod("Mobile Banking", "bKash", "Md. Mahmudul Hossain", "***** - *** - 300", null, null, null))
            paymentMethodList.add(PaymentMethod("Mobile Banking", "Rocket", "John Doe the Great", "***** - *** - 342", null, null, null))

            return paymentMethodList
        }
        return listOf()
    }
}