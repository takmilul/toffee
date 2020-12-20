package com.banglalink.toffee.ui.category.drama

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo

class EpisodeListAdapter(callback: BaseListItemCallback<ChannelInfo>?,
                         private var selectedChannel: ChannelInfo? = null):
    BasePagingDataAdapter<ChannelInfo>(callback, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_episode_list
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val obj = getItem(position)
        obj?.let {
            holder.bind(obj, callback, position, selectedChannel)
        }
    }

    fun setSelectedItem(channelInfo: ChannelInfo?) {
        selectedChannel = channelInfo
        notifyDataSetChanged()
    }
}