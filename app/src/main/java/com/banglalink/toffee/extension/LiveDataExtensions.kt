package com.banglalink.toffee.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.banglalink.toffee.exception.Error
import com.banglalink.toffee.model.Resource

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, body: (T) -> Unit = {}) {
    liveData.observe(this, Observer { it?.let { t -> body(t) } })
}

fun <T> MutableLiveData<T>.toLiveData() = this as LiveData<T>

fun <T> MutableLiveData<Resource<T>>.setSuccess(data: T) =
    postValue(Resource.Success(data))


fun <T> MutableLiveData<Resource<T>>.setError(error:Error) =
    postValue(Resource.Failure(error))