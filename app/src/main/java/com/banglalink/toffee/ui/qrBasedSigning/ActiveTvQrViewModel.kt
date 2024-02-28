package com.banglalink.toffee.ui.qrBasedSigning

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.QrSignInService
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ActiveTvQrViewModel @Inject constructor(

    private val qrSignInService: QrSignInService
):ViewModel() {

    var qrSignInStatus = SingleLiveEvent<Int>()


    fun getSubscriberPaymentInit(code: String) {
        viewModelScope.launch {
            var response =  qrSignInService.execute(code)
            Log.d("TAG", "execute121212: "+response)
            qrSignInStatus.value=response
        }
    }
}