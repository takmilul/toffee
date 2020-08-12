package com.banglalink.toffee.ui.useractivities

import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.home.OptionCallBack
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class UserActivitiesListAdapter(private val optionCallback: OptionCallBack,
                                channelCallback: (ChannelInfo)->Unit = {}):
    MyBaseAdapter<ChannelInfo>(channelCallback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.tab_activities_list_item_layout
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bindCallBack(optionCallback)
    }
}