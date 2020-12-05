package com.banglalink.toffee.ui.trendingchannels

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.TrendingChannelInfo
import com.banglalink.toffee.ui.landing.LandingPopularChannelCallback

class TrendingChannelsListAdapter(cb: BaseListItemCallback<TrendingChannelInfo>)
    :BasePagingDataAdapter<TrendingChannelInfo>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_trending_channels
    }
}