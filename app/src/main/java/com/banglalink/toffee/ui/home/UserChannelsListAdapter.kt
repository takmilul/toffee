package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ListItemCategoriesBinding
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelSubscriptionInfo
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class UserChannelsListAdapter(private val optionCallBack: OptionCallBack,
                              channelCallback:(ChannelSubscriptionInfo)->Unit={}):
    MyBaseAdapter<ChannelSubscriptionInfo>(channelCallback) {

    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_user_channels
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bindCallBack(optionCallBack)
    }
}