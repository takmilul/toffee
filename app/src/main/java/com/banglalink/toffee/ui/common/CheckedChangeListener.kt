package com.banglalink.toffee.ui.common

import android.view.View
import com.banglalink.toffee.common.paging.BaseListItemCallback

interface CheckedChangeListener<T : Any>: BaseListItemCallback<T> {
    fun onCheckedChanged(view: View, item: T, position: Int, isFromCheckableView: Boolean) {}
}