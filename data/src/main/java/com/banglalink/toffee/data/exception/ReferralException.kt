package com.banglalink.toffee.data.exception

data class ReferralException(val errorCode:Int, val referralStatusMessage: String, val referralStatus: String):Exception(referralStatusMessage)