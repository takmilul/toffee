package com.banglalink.toffee.ui.useractivities

import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.ChannelSubscriptionInfo
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.common.SingleListItemCallback
import com.banglalink.toffee.ui.home.OptionCallBack
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class UserActivitiesListAdapter(callback: SingleListItemCallback<ChannelInfo>):
    MyBaseAdapterV2<ChannelInfo>(callback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.tab_activities_list_item_layout
    }
}