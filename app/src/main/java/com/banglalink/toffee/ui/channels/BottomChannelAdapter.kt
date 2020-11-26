package com.banglalink.toffee.ui.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.bindChannel

class BottomChannelAdapter(private val callback: BaseListItemCallback<TVChannelItem>)
    :PagingDataAdapter<TVChannelItem, BottomChannelViewHolder>(ItemComparator()) {

    private var selectedItem: ChannelInfo? = null

    override fun onBindViewHolder(holder: BottomChannelViewHolder, position: Int) {
        val obj = getItem(position)
        obj?.let {
            if(it.channelInfo != null) {
                bindChannel(holder.imageView, it.channelInfo)
                if (it.channelInfo.id == selectedItem?.id.toString()) {
                    holder.imageView.borderWidth = Utils.dpToPx(4)
                } else {
                    holder.imageView.borderWidth = 0
                }
            }
            holder.itemView.setOnClickListener {
                callback.onItemClicked(obj)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomChannelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return BottomChannelViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.fragment_bottom_channel_item_layout
    }

    fun setSelectedItem(item: ChannelInfo?) {
        selectedItem = item
        notifyDataSetChanged()
    }
}