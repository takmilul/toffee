package com.banglalink.toffee.ui.channels

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.ui.common.SimpleRecyclerAdapter

class RecentChannelsAdapter(private val callback: BaseListItemCallback<TVChannelItem>)
    :SimpleRecyclerAdapter<TVChannelItem>(callback = callback) {

//    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
//        val obj = getItem(position)
//        obj?.channelInfo?.let {
//            holder.bind(it, callback, position)
//        }
//    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_live_recently_viewed
    }
}