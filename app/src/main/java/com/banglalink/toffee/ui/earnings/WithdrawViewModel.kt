package com.banglalink.toffee.ui.earnings

import com.banglalink.toffee.model.PaymentMethod
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel
import com.banglalink.toffee.usecase.GetPaymentMethod

class WithdrawViewModel: SingleListViewModel<PaymentMethod>() {
    override var repo: SingleListRepository<PaymentMethod> = GetPaymentMethod()

    /*private val _listData = MutableLiveData<Resource<List<PaymentMethod>>>()
    var listData = _listData.toLiveData()

    private val _data = MutableLiveData<List<PaymentMethod>>()
    var data = _data.toLiveData()
    
    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress = _showProgress.toLiveData()

    var enableToolbar = true

    init {
        _showProgress.value = false
    }

    fun loadData() {
        _showProgress.value = true
        viewModelScope.launch {
            _listData.value = setAndResultFromResponse(_data) { repo.execute() }
            _showProgress.value = false
        }
    }*/

}