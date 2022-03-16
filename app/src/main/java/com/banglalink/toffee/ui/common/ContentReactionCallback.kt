package com.banglalink.toffee.ui.common

import android.view.View
import com.banglalink.toffee.common.paging.ProviderIconCallback

interface ContentReactionCallback<T : Any>: ProviderIconCallback<T> {
    fun onReactionClicked(view: View, reactionCountView: View, item: T) {}
    fun onShareClicked(view: View, item: T, isPlaylist: Boolean = false) {}
    fun onSubscribeButtonClicked(view: View, item: T) {}
}

interface ReactionIconCallback{
    fun onReactionChange(reactionCount: String, reactionText: String, reactionIcon: Int)
}