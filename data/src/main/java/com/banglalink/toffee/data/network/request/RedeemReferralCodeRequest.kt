package com.banglalink.toffee.data.network.request

class RedeemReferralCodeRequest(val referralCode: String, val customerId: Int, val password: String) :
    BaseRequest(apiName = "redeemReferralCode")