package com.banglalink.toffee.exception

class CustomerNotFoundException(val errorCode: Int, val errorMessage:String):Exception(errorMessage)