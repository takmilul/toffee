package com.banglalink.toffee.ui.landing

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ListItemVideosBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.ContentReactionCallback

class LatestVideosAdapter(
    val cb: ContentReactionCallback<ChannelInfo>,
) : BasePagingDataAdapter<ChannelInfo>(cb as BaseListItemCallback<ChannelInfo>, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_videos
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        if (holder.binding is ListItemVideosBinding) {
            holder.binding.poster.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}