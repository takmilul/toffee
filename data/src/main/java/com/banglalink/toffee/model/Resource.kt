package com.banglalink.toffee.model

import com.banglalink.toffee.exception.Error
import com.google.gson.annotations.SerializedName


sealed class Resource<out T> {
    data class Success<out T>(
        @SerializedName("data")
        val data: T
    ) : Resource<T>()
    
    data class Failure<out T>(
        @SerializedName("error")
        val error: Error
    ) : Resource<T>()
}