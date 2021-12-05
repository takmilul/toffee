package com.banglalink.toffee.data.exception

class ContentExpiredException(val errorCode: Int, val errorMessage:String):Exception(errorMessage)