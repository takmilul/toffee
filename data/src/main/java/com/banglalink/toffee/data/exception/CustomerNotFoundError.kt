package com.banglalink.toffee.data.exception

class CustomerNotFoundError(
    code: Int,
    msg: String,
) : Error(code, msg)