package com.banglalink.toffee.listeners

import android.view.View
import com.banglalink.toffee.common.paging.BaseListItemCallback

interface DataPackOptionCallback<T: Any>: BaseListItemCallback<T> {
    fun onItemClicked(view: View, item: T, position: Int) {}
    fun onCheckedChangeListener(view: View, isChecked: Boolean, item: T, position: Int) {}
}