package com.banglalink.toffee.ui.landing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ListItemVideosBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.ContentReactionCallback

class LatestVideosAdapter(
    val cb: ContentReactionCallback<ChannelInfo>,
) : PagingDataAdapter<ChannelInfo, ViewHolder>(ItemComparator()) {
    
    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_videos
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater, viewType, parent, false
        )
        return BaseViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val obj = getItem(position)
        obj?.let {
            (holder as BaseViewHolder).bind(obj, cb, position)
        }
    }
    
    fun getItemByIndex(idx: Int): ChannelInfo? {
        return getItem(idx)
    }
    
    override fun onViewRecycled(holder: ViewHolder) {
        if (holder is BaseViewHolder && holder.binding is ListItemVideosBinding) {
            holder.binding.poster.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}