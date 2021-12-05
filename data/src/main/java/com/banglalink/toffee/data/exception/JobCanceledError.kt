package com.banglalink.toffee.data.exception

class JobCanceledError(
    code: Int,
    msg: String,
) : Error(code, msg)