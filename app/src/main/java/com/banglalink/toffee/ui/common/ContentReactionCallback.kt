package com.banglalink.toffee.ui.common

import android.view.View
import android.widget.TextView
import com.banglalink.toffee.common.paging.BaseListItemCallback

interface ContentReactionCallback<T : Any>: BaseListItemCallback<T> {
    fun onReactionClicked(view: View, position: Int, item: T, textView: TextView) {}
    fun onShareClicked(view: View, item: T) {}
}