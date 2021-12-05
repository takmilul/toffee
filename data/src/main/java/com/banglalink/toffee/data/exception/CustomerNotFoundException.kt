package com.banglalink.toffee.data.exception

class CustomerNotFoundException(val errorCode: Int, val errorMessage:String):Exception(errorMessage)