package com.banglalink.toffee.exception

import java.lang.Exception

data class ReferralException(val errorCode:Int, val referralStatusMessage: String, val referralStatus: String):Exception(referralStatusMessage)