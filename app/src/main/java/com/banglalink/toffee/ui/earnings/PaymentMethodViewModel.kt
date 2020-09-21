package com.banglalink.toffee.ui.earnings

import com.banglalink.toffee.model.PaymentMethod
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel
import com.banglalink.toffee.usecase.GetPaymentMethod

class PaymentMethodViewModel: SingleListViewModel<PaymentMethod>() {
    override var repo: SingleListRepository<PaymentMethod> = GetPaymentMethod()
}