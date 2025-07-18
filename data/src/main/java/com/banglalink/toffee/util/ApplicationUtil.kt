package com.banglalink.toffee.util

import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.exception.ApiException
import com.banglalink.toffee.data.exception.AppDeprecatedError
import com.banglalink.toffee.data.exception.CustomerNotFoundError
import com.banglalink.toffee.data.exception.CustomerNotFoundException
import com.banglalink.toffee.data.exception.Error
import com.banglalink.toffee.data.exception.JobCanceledError
import com.banglalink.toffee.data.exception.ReferralException
import com.banglalink.toffee.data.exception.UpdateRequiredException
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
            return Error(e.code(), e.message().ifBlank { "High user traffic. Please try again after some time." })
        }
        is SocketTimeoutException -> {
            return Error(-1, "High user traffic. Please try again after some time.")
        }
        is IOException -> {
            return Error(-1, "Unable to connect server.")
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

fun getExternalError(e: Exception): Error {
    e.printStackTrace()
    ToffeeAnalytics.logException(e)
    when (e) {
        is HttpException -> {
            return Error(e.code(), e.message().ifBlank { "Unknown error occurred" })
        }
        is SocketTimeoutException -> {
            return Error(-1, "Unable to connect server.")
        }
        is IOException -> {
            return Error(-1, "Unable to connect server.")
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