package com.banglalink.toffee.common.paging

import android.view.View

interface ProviderIconCallback<T: Any>: BaseListItemCallback<T> {
    fun onProviderIconClicked(item: T){}
    fun onSubscribeButtonClicked(view: View, item: T) {}
}