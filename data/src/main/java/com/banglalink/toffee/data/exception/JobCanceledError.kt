package com.banglalink.toffee.exception

class JobCanceledError(
    code: Int,
    msg: String,
) : Error(code, msg)