package com.banglalink.toffee.data.network.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.banglalink.toffee.Constants.MULTI_DEVICE_LOGIN_ERROR_CODE
import com.banglalink.toffee.Constants.OUTSIDE_OF_BD_ERROR_CODE
import com.banglalink.toffee.Constants.UN_ETHICAL_ACTIVITIES_ERROR_CODE
import com.banglalink.toffee.data.network.response.BaseResponse
import com.banglalink.toffee.data.exception.ApiException
import com.banglalink.toffee.data.exception.CustomerNotFoundException
import com.banglalink.toffee.data.exception.OutsideOfBDException
import com.banglalink.toffee.data.exception.UnEthicalActivitiesException
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.EventProvider
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.Dispatchers

suspend fun <T : BaseResponse> tryIO2(block: suspend () -> T): T {
    val response = block()
    return when{
        response.errorCode == UN_ETHICAL_ACTIVITIES_ERROR_CODE -> {
            EventProvider.post(UnEthicalActivitiesException(response.errorMsg ?: "You are trying to do unethical activities"))
            throw ApiException(
                response.errorCode,
                response.errorMsg ?: "You are trying to do unethical activity"
            )
        }
        response.errorCode == OUTSIDE_OF_BD_ERROR_CODE -> {
            EventProvider.post(OutsideOfBDException(response.errorMsg ?: "You are trying to connect out of Bangladesh"))
            throw ApiException(
                response.errorCode,
                response.errorMsg ?: "Toffee is available only in Bangladesh"
            )
        }
        response.errorCode == MULTI_DEVICE_LOGIN_ERROR_CODE -> {
            EventProvider.post(CustomerNotFoundException(response.errorCode, "Customer multiple login occurred"))
            throw CustomerNotFoundException(
                response.errorCode,
                ""//we do not want to show error message for this error code
            )
        }
        response.status == 1 ->{//server suffered a serious error
            throw ApiException(
                response.errorCode,
                response.errorMsg ?:"Something went wrong. Please try again."
            )
        }
        response.errorCode != 0 ->{//hmmm....error occurred ....throw it
            throw ApiException(
                response.errorCode,
                response.errorMsg ?:"Something went wrong. Please try again."
            )
        }
        else->{
            response//seems like all fine ...return the body
        }
    }

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
