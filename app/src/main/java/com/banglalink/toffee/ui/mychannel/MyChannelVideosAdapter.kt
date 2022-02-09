package com.banglalink.toffee.ui.mychannel

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ListItemVideosBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.ContentReactionCallback

class MyChannelVideosAdapter(
    listener: ContentReactionCallback<ChannelInfo>?,
) : BasePagingDataAdapter<ChannelInfo>(listener as BaseListItemCallback<ChannelInfo>, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_my_channel_videos
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        if (holder.binding is ListItemVideosBinding) {
            holder.binding.poster.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}