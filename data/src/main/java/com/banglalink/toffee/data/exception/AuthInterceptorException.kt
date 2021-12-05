package com.banglalink.toffee.data.exception

import java.io.IOException

class AuthInterceptorException(msg: String?, cause: Throwable?): IOException(msg, cause)