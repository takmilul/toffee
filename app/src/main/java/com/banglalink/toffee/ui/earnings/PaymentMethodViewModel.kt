package com.banglalink.toffee.ui.earnings

import com.banglalink.toffee.apiservice.GetPaymentMethod
import com.banglalink.toffee.model.PaymentMethod
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel

class PaymentMethodViewModel: SingleListViewModel<PaymentMethod>() {
    override var repo: SingleListRepository<PaymentMethod> = GetPaymentMethod()
}