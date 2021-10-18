package com.banglalink.toffee.data.network.request

data class TermsConditionRequest (
    var customerId:Int,
    var password:String
): BaseRequest("getUgcTermsAndConditions")