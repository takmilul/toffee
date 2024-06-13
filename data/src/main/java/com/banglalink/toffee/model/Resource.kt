package com.banglalink.toffee.model

import com.banglalink.toffee.data.exception.Error
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class Resource<out T> {
    @Serializable
    data class Success<out T>(
        @SerialName("data")
        val data: T
    ) : Resource<T>()
    
    @Serializable
    data class Failure<out T>(
        @SerialName("error")
        val error: Error
    ) : Resource<T>()
}