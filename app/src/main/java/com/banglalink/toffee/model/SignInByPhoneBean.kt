package com.banglalink.toffee.model

import com.banglalink.toffee.data.network.response.BodyResponse

data class SignInByPhoneBean(val authorize:Boolean, val regSessionToken:String):BodyResponse()