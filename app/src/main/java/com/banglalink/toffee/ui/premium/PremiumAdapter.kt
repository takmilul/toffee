package com.banglalink.toffee.ui.premium

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo

class PremiumAdapter(
    cb: PremiumFragment
) : BasePagingDataAdapter<ChannelInfo>(cb, ItemComparator()) {
    
    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_premium
    }
}