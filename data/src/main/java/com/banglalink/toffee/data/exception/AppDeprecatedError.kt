package com.banglalink.toffee.data.exception

class AppDeprecatedError(
    code: Int,
    msg: String,
    val title: String,
    val updateMsg: String,
    val forceUpdate: Boolean
) : Error(code, msg)