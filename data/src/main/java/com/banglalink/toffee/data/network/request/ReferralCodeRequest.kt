package com.banglalink.toffee.data.network.request

data class ReferralCodeRequest(val customerId: Int, val password: String) :
    BaseRequest("getMyReferralCode")