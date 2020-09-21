package com.banglalink.toffee.ui.common

import android.view.View

interface SingleListItemCallback<T: Any> {
    fun onItemClicked(item: T) {}
    fun onOpenMenu(item: T) {}
    fun onCheckedChanged(view: View, item: T) {}
}