package com.banglalink.toffee.ui.login

import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.SignInByPhone
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel2 @Inject constructor(
    private val pref: SessionPreference,
    private val signInByPhone: SignInByPhone,
) : BaseViewModel() {

    val signByPhoneResponse = SingleLiveEvent<Resource<Any>>()

    fun signIn(phoneNumber: String) {
        viewModelScope.launch {
            val response= resultFromResponse {  signInByPhone.execute(phoneNumber, "") }
            signByPhoneResponse.value=response
        }
    }
}