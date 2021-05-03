package com.banglalink.toffee.exception

class CustomerNotFoundError(
    code: Int,
    msg: String,
) : Error(code, msg)