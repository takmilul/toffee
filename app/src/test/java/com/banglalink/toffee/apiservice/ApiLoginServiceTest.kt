package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.mockito.Mock


class ApiLoginServiceTest{

    @Mock
    var mockAuthApi: AuthApi = mock()

    @Test
    fun api_login_success(){

    }

    @Test
    fun api_login_no_account_found(){

    }
}