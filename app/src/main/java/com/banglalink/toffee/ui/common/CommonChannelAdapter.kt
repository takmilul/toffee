package com.banglalink.toffee.ui.common

import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.listeners.OptionCallBack

class CommonChannelAdapter(private val optionCallBack: OptionCallBack, channelCallback:(ChannelInfo)->Unit={}): MyBaseAdapter<ChannelInfo>(channelCallback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        if(getItem(position)!!.isLive){
            return R.layout.list_item_live
        }
        return R.layout.list_item_catchup
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bindCallBack(optionCallBack)
    }
}