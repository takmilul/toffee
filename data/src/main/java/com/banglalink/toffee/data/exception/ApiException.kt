package com.banglalink.toffee.data.exception

data class ApiException(val errorCode:Int,val errorMessage: String):Exception(errorMessage)