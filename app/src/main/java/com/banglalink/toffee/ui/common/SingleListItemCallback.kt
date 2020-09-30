package com.banglalink.toffee.ui.common

import android.view.View

interface SingleListItemCallback<T: Any> {
    fun onItemClicked(item: T) {}
    fun onOpenMenu(anchor: View, item: T) {}
}