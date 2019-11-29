package com.banglalink.toffee.util

import androidx.lifecycle.LiveData

object EventProvider {

    private val liveDataEvent = SingleLiveEvent<Event>()

    fun post(event: Any) {
        this.liveDataEvent.postValue(Event(event))
    }

    fun getEventLiveData(): LiveData<Event> {
        return liveDataEvent
    }


    class Event(private val value:Any) {
        fun getValue(): Any {
            return this.value
        }
    }
}