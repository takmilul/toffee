package com.banglalink.toffee.ui.channels

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.BindingUtil

class BottomChannelAdapter(
    private val context: Context,
    private val bindingUtil: BindingUtil,
    private val callback: BaseListItemCallback<ChannelInfo>
) : PagingDataAdapter<ChannelInfo, BottomChannelViewHolder>(ItemComparator()) {
    
    private var selectedItem: ChannelInfo? = null
    
    override fun onBindViewHolder(holder: BottomChannelViewHolder, position: Int) {
        val obj = getItem(position)
        obj?.let {
            bindingUtil.bindChannel(holder.imageView, it)
//            holder.premiumIcon.isVisible = it.urlTypeExt == 1
            if (it.id == selectedItem?.id.toString()) {
                holder.imageView.background = ContextCompat.getDrawable(context, R.drawable.selected_channel_bg)
            } else {
                holder.imageView.background = ContextCompat.getDrawable(context, R.drawable.circular_white_bg)
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
        return R.layout.live_tv_item
    }
    
    fun setSelectedItem(item: ChannelInfo?) {
        selectedItem = item
        notifyDataSetChanged()
    }
}