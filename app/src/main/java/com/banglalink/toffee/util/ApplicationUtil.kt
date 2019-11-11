package com.banglalink.toffee.util

import android.text.TextUtils
import com.banglalink.toffee.exception.ApiException
import com.banglalink.toffee.exception.Error
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException

inline fun <T : Any, R> modelMapper(input: T, block: (T) -> R): R {
    return block(input)
}

fun getError(e: Exception): Error {
    e.printStackTrace()
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
        else -> {
            return Error(-1, "Unknown error occurred")
        }
    }
}

fun discardZeroFromDuration(duration: String): String {
    val retValue: String
    if (TextUtils.isEmpty(duration)) {
        retValue = "00:00"
        return retValue
    }

    if (duration.startsWith("00:")) {
        retValue = duration.substring(3)
    } else {
        retValue = duration
    }

    return retValue
}

private val c = charArrayOf('K', 'M', 'B', 'T')

/**
 * Recursive implementation, invokes itself for each factor of a thousand, increasing the class on each invokation.
 *
 * @param n         the number to format
 * @param iteration in fact this is the class from the array c
 * @return a String representing the number n formatted in a cool looking way.
 */
private fun viewCountFormat(n: Double, iteration: Int): String {
    val d = n.toLong() / 100 / 10.0
    val isRound =
        d * 10 % 10 == 0.0//true if the decimal part is equal to 0 (then it's trimmed anyway)
    return (if (d < 1000)
    //this determines the class, i.e. 'k', 'm' etc
        ((if (d > 99.9 || isRound || (!isRound && d > 9.99))
        //this decides whether to trim the decimals
            d.toInt() * 10 / 10
        else
            (d).toString() + "" // (int) d * 10 / 10 drops the decimal
                )).toString() + "" + c[iteration]
    else
        viewCountFormat(d, iteration + 1))

}

fun getFormattedViewsText(viewCount: String): String {

    if (TextUtils.isEmpty(viewCount) || !TextUtils.isDigitsOnly(viewCount)) return viewCount

    val count = java.lang.Long.parseLong(viewCount)
    return if (count < 1000)
        viewCount
    else
        viewCountFormat(count.toDouble(), 0)
}