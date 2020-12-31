package com.banglalink.toffee.ui.mychannel

import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.MyChannelPlaylist

class MyChannelPlaylistAdapter(callback: BaseListItemCallback<MyChannelPlaylist>?): BasePagingDataAdapter<MyChannelPlaylist>(callback, ItemComparator()) {
    var isOwner: Int = 0
    
    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_my_channel_playlist
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val obj = getItem(position)
        
        holder.binding.setVariable(BR.isOwner, isOwner)
        holder.bind(obj!!, callback, position)
    }
}