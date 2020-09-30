package com.banglalink.toffee.data.network.interceptor

interface IGetMethodTracker {

    fun shouldConvertToGetRequest(urlEncodedFragmentString: String):Boolean
}