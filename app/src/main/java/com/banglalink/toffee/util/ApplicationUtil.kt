package com.banglalink.toffee.util

import android.text.TextUtils
import com.banglalink.toffee.exception.ApiException
import com.banglalink.toffee.exception.AppDeprecatedError
import com.banglalink.toffee.exception.Error
import com.banglalink.toffee.exception.UpdateRequiredException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException

//this will use non synchronized lazy method
fun <T>unsafeLazy(initializer: () -> T): Lazy<T>{
    return lazy(LazyThreadSafetyMode.NONE){
        initializer()
    }
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
        is UpdateRequiredException->{
            return AppDeprecatedError(-1,"",e.title,e.updateMsg,e.forceUpdate)
        }
        else -> {
            return Error(-1, "Unknown error occurred")
        }
    }
}

suspend fun discardZeroFromDuration(duration: String): String {
    return withContext(Dispatchers.Default){
        if (TextUtils.isEmpty(duration)) {
           return@withContext "00:00"
        }

        if (duration.startsWith("00:")) {
            duration.substring(3)
        } else {
            duration
        }
    }
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

suspend fun getFormattedViewsText(viewCount: String): String {

    return withContext(Dispatchers.Default){
        if (TextUtils.isEmpty(viewCount) || !TextUtils.isDigitsOnly(viewCount))
            return@withContext viewCount

        val count = java.lang.Long.parseLong(viewCount)
        if (count < 1000)
            viewCount
        else
            viewCountFormat(count.toDouble(), 0)
    }

}