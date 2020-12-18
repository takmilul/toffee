package com.banglalink.toffee.common.paging

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

class ListLoadStateAdapter(
        private val retry: () -> Unit
) : LoadStateAdapter<ListLoadStateViewHolder>() {
    override fun onBindViewHolder(holder: ListLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ListLoadStateViewHolder {
        return ListLoadStateViewHolder.create(parent, retry)
    }
}
