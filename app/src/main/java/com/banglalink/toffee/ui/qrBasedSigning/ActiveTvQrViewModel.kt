package com.banglalink.toffee.ui.qrBasedSigning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.QrSignInService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveTvQrViewModel @Inject constructor(
    private val qrSignInService: QrSignInService,
) : ViewModel() {
    var qrSignInStatus = SingleLiveEvent<Resource<Int>>()
    
    fun getSubscriberPaymentInit(code: String) {
        viewModelScope.launch {
            val response = resultFromResponse { qrSignInService.execute(code) }
            qrSignInStatus.value = response
        }
    }
}