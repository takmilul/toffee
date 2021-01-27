package com.banglalink.toffee.ui.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.SimpleRecyclerAdapter
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.bindChannel

class RecentChannelsAdapter(private val callback: BaseListItemCallback<TVChannelItem>)
    :SimpleRecyclerAdapter<TVChannelItem>(callback = callback) {

//    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
//        val obj = getItem(position)
//        obj?.channelInfo?.let {
//            holder.bind(it, callback, position)
//        }
//    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.fragment_recent_tv_item_layout
    }
}