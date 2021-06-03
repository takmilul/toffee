package com.banglalink.toffee.exception

class ContentExpiredException(val errorCode: Int, val errorMessage:String):Exception(errorMessage)