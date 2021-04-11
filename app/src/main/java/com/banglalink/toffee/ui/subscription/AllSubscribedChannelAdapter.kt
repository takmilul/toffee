package com.banglalink.toffee.ui.subscription

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.listeners.LandingPopularChannelCallback
import com.banglalink.toffee.model.UserChannelInfo

class AllSubscribedChannelAdapter (cb: LandingPopularChannelCallback<UserChannelInfo>)
    : BasePagingDataAdapter<UserChannelInfo>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_all_subscribed_channels
    }
}