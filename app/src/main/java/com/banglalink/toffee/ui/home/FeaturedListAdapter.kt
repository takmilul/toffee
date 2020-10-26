package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo

class FeaturedListAdapter(cb: BaseListItemCallback<ChannelInfo>)
    :BasePagingDataAdapter<ChannelInfo>(cb, ItemComparator()){

    override fun getItemViewType(position: Int): Int {
        return R.layout.landing_featured_item_layout
    }
}