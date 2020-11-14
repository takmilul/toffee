package com.banglalink.toffee.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.MyViewHolderV2

class ChannelHeaderAdapter(private var channelInfo: ChannelInfo,
                           private val cb: ContentReactionCallback<ChannelInfo>? = null)
    :RecyclerView.Adapter<MyViewHolderV2>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderV2 {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater, viewType, parent, false
        )
        return MyViewHolderV2(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.catchup_details_list_header_new
    }

    override fun onBindViewHolder(holder: MyViewHolderV2, position: Int) {
        holder.bind(channelInfo, cb, position)
    }

    override fun getItemCount(): Int {
        return 1
    }

    fun setChannelInfo(info: ChannelInfo) {
        channelInfo = info
        notifyItemChanged(0)
    }
}