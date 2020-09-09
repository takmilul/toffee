package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class UserChannelsListAdapter(private val optionCallBack: OptionCallBack,
                              channelCallback:(ChannelInfo)->Unit={}):
    MyBaseAdapter<ChannelInfo>(channelCallback) {

    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_user_channels
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bindCallBack(optionCallBack)
    }
}