package com.banglalink.toffee.ui.common

interface SingleListItemCallback<T: Any> {
    fun onItemClicked(item: T) {}
    fun onOpenMenu(item: T) {}
}