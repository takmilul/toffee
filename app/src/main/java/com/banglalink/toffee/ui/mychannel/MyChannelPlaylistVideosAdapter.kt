package com.banglalink.toffee.ui.mychannel

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo

class MyChannelPlaylistVideosAdapter(
    callback: BaseListItemCallback<ChannelInfo>?,
    private var selectedItem: ChannelInfo? = null,
) : BasePagingDataAdapter<ChannelInfo>(callback, ItemComparator()) {
    
    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_my_channel_playlist_videos
    }
    
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val obj = getItem(position)
        obj?.let {
            holder.bind(obj, callback, position, selectedItem)
        }
    }
    
    fun setSelectedItem(item: ChannelInfo?) {
        selectedItem = item
        notifyDataSetChanged()
    }
}