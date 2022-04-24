package com.banglalink.toffee.util

import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.exception.*
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.CancellationException

//this will use non synchronized lazy method
fun <T>unsafeLazy(initializer: () -> T): Lazy<T>{
    return lazy(LazyThreadSafetyMode.NONE){
        initializer()
    }
}

fun getError(e: Exception): Error {
    e.printStackTrace()
    ToffeeAnalytics.logException(e)
    when (e) {
        is HttpException -> {
            return Error(e.code(), e.message())
        }
        is IOException -> {
            return Error(-1, "Unable to connect server")
        }
        is SocketTimeoutException -> {
            return Error(-1, "Connection time out")
        }
        is ApiException -> {
            return Error(e.errorCode, e.errorMessage)
        }
        is ReferralException -> {
            return Error(e.errorCode, e.referralStatusMessage, e.referralStatus)
        }
        is UpdateRequiredException -> {
            return AppDeprecatedError(-1, "", e.title, e.updateMsg, e.forceUpdate)
        }
        is CustomerNotFoundException -> {
            return CustomerNotFoundError(e.errorCode, e.errorMessage)
        }
        is CancellationException -> {
            return JobCanceledError(-1, "Unknown error occurred")
        }
        else -> {
            return Error(-1, "Unknown error occurred")
        }
    }
}