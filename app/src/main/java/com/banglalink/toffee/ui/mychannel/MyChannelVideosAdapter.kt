package com.banglalink.toffee.ui.mychannel

import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.ContentReactionCallback

class MyChannelVideosAdapter(listener: ContentReactionCallback<ChannelInfo>?): BasePagingDataAdapter<ChannelInfo>(listener as BaseListItemCallback<ChannelInfo>, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_my_channel_videos
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.binding.setVariable(BR.isMyChannel, true)
        /*holder.itemView.findViewById<TextView>(R.id.reactionButton)?.setOnLongClickListener { 
            listener?.onReactionLongPressed(it, holder.itemView.reactionCount, getItem(position)!!)
            true
        }*/
    }
}