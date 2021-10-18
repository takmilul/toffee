package com.banglalink.toffee.ui.earnings

import com.banglalink.toffee.model.PaymentMethod
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel
import java.lang.UnsupportedOperationException

class WithdrawViewModel: SingleListViewModel<PaymentMethod>() {
    override var repo: SingleListRepository<PaymentMethod> = throw UnsupportedOperationException("Use new api system")
}