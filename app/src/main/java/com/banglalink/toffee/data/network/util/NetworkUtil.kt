package com.banglalink.toffee.data.network.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.network.response.BaseResponse
import com.banglalink.toffee.exception.ApiException
import com.banglalink.toffee.exception.CustomerNotFoundException
import com.banglalink.toffee.model.MULTI_DEVICE_LOGIN_ERROR_CODE
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.EventProvider
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.Dispatchers
import retrofit2.Response

suspend fun <T : BaseResponse> tryIO(block: suspend () -> Response<T>): T {
    val response: Response<T> = block()
    if (response.isSuccessful) {
        response.body()?.let {
           return when{
                it.errorCode == MULTI_DEVICE_LOGIN_ERROR_CODE->{
                    ToffeeAnalytics.logApiError(it.apiName,it.errorMsg)
                    EventProvider.post(CustomerNotFoundException("Customer multiple login occurred"))
                    throw ApiException(
                        it.errorCode,
                        ""//we do not want to show error message for this error code
                    )
                }
               it.status == 1 ->{//server suffered a serious error
                   ToffeeAnalytics.logApiError(it.apiName,it.errorMsg)
                   throw ApiException(
                       it.status,
                       it.errorMsg ?:"Something went wrong. Please try again later"
                   )
               }
                it.errorCode!=0->{//hmmm....error occurred ....throw it
                    ToffeeAnalytics.logApiError(it.apiName,it.errorMsg)
                    throw ApiException(
                        it.errorCode,
                        it.errorMsg ?:"Unknown error occurred"
                    )
                }
                else->{
                    it//seems like all fine ...return the body
                }
            }
        }
    }
    throw ApiException(response.code(),response.message())

}

fun <T> resultLiveData(networkCall: suspend () -> T): LiveData<Resource<T>> =
    liveData(Dispatchers.IO) {
        try {
            val response = networkCall.invoke()
            emit(Resource.Success(response))

        }catch (e:Exception){
            emit(Resource.Failure<T>(getError(e)))
        }
    }

suspend fun <T> resultFromResponse(networkCall: suspend () -> T): Resource<T> =
    try {
        val response = networkCall.invoke()
        Resource.Success(response)

    }catch (e:Exception){
        Resource.Failure<T>(getError(e))
    }
