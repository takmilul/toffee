package com.banglalink.toffee.data.network.request

abstract class BasePagingRequest(apiName: String): BaseRequest(apiName) {
    abstract val offset: Int
    abstract val limit: Int
}
// Can be replaced with interface