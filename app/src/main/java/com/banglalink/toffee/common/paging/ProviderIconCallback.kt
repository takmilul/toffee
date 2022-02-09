package com.banglalink.toffee.common.paging

interface ProviderIconCallback<T: Any>: BaseListItemCallback<T> {
    fun onProviderIconClicked(item: T){}
}