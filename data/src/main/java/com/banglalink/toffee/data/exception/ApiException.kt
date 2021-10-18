package com.banglalink.toffee.exception

import java.lang.Exception

data class ApiException(val errorCode:Int,val errorMessage: String):Exception(errorMessage)