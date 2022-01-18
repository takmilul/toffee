package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.model.ChannelInfo

class CatchUpDetailsAdapter(
    cb: ProviderIconCallback<ChannelInfo>
): BasePagingDataAdapter<ChannelInfo>(cb, ItemComparator()) {
    override fun getItemViewType(position: Int): Int {
        if(getItem(position)?.isLinear == true){
            return R.layout.list_item_live_new
        }
        return R.layout.list_item_relative
    }
}