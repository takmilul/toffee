package com.banglalink.toffee.util

import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.exception.*
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

fun discardZeroFromDuration(duration: String?): String {
    if (duration.isNullOrBlank()) {
       return "00:00"
    }

    return if (duration.startsWith("00:")) {
        duration.substring(3)
    } else {
        duration
    }
}

private val c = charArrayOf('K', 'M', 'B', 'T', 'P', 'E')

/**
 * Recursive implementation, invokes itself for each factor of a thousand, increasing the class on each invokation.
 *
 * @param n         the number to format
 * @param iteration in fact this is the class from the array c
 * @return a String representing the number n formatted in a cool looking way.
 */
private fun viewCountFormat(n: Double, iteration: Int): String {
    val d = n.toLong() / 100 / 10.0
    val isRound = d * 10 % 10 == 0.0
    return if (d < 1000)
        ((if (d > 99.9 || isRound || (!isRound && d > 9.99))
            d.toInt() * 10 / 10
        else
            (d).toString() + "" // (int) d * 10 / 10 drops the decimal
                )).toString() + "" + c[iteration]
    else
        viewCountFormat(d, iteration + 1)

}

fun getFormattedViewsText(viewCount: String?): String {
    if (viewCount.isNullOrBlank())
        return "0"

    val count = viewCount.toLong()
    return if (count < 1000)
        viewCount
    else
        viewCountFormat(count.toDouble(), 0)
}