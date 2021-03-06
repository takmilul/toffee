package com.banglalink.toffee.ui.userchannels

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.UserChannelInfo
import com.banglalink.toffee.listeners.LandingPopularChannelCallback

class AllUserChannelsListAdapter(cb: LandingPopularChannelCallback<UserChannelInfo>)
    :BasePagingDataAdapter<UserChannelInfo>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_all_user_channels
    }
}