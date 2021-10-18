package com.banglalink.toffee.model

import com.banglalink.toffee.exception.Error


sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Failure<out T>(val error: Error) : Resource<T>()
}