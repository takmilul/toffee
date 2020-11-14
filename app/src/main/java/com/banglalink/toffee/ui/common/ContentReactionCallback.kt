package com.banglalink.toffee.ui.common

import android.view.View
import com.banglalink.toffee.common.paging.BaseListItemCallback

interface ContentReactionCallback<T : Any>: BaseListItemCallback<T> {
    fun onReactionClicked(view: View, item: T) {}
    fun onShareClicked(view: View, item: T) {}
    fun onSubscribeButtonClicked(view: View, item: T) {}
}