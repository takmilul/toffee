package com.banglalink.toffee.ui.search

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.model.ChannelInfo

class SearchAdapter(listener: ProviderIconCallback<ChannelInfo>) : BasePagingDataAdapter<ChannelInfo>(listener, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)!!.isLinear) {
            R.layout.list_item_live_new
        } else if (getItem(position)!!.isChannel) {
            R.layout.list_item_ugc_new
        } else R.layout.list_item_relative
    }
}