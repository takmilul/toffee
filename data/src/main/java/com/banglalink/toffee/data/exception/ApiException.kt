package com.banglalink.toffee.data.exception

import kotlinx.serialization.Serializable

@Serializable
data class ApiException(val errorCode:Int,val errorMessage: String):Exception(errorMessage)