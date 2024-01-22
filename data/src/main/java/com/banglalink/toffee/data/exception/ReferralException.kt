package com.banglalink.toffee.data.exception

import kotlinx.serialization.Serializable

@Serializable
data class ReferralException(val errorCode:Int, val referralStatusMessage: String, val referralStatus: String):Exception(referralStatusMessage)