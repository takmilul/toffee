package com.banglalink.toffee.ui.common

import android.view.View
import android.widget.TextView
import com.banglalink.toffee.common.paging.ProviderIconCallback

interface ContentReactionCallback<T : Any>: ProviderIconCallback<T> {
    fun onReactionClicked(view: View, reactionCountView: View, item: T) {}
    fun onReactionLongPressed(view: View, reactionCountView: View, item: T) {}
    fun onShareClicked(view: View, item: T) {}
    fun onSubscribeButtonClicked(view: View, item: T) {}
}