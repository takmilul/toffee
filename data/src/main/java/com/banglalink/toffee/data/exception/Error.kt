package com.banglalink.toffee.data.exception

import kotlinx.serialization.Serializable

@Serializable
open class Error(val code : Int, val msg : String, val additionalMsg: String = "")