package com.banglalink.toffee.common.paging

import android.view.View

interface BaseListItemCallback<T: Any> {
    fun onItemClicked(item: T) {}
    fun onOpenMenu(view: View, item: T) {}
}