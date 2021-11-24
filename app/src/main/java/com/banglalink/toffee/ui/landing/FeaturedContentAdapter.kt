package com.banglalink.toffee.ui.landing

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.MyBaseAdapter

class FeaturedContentAdapter(val listener: BaseListItemCallback<ChannelInfo>): MyBaseAdapter<ChannelInfo>(listener) {
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.landing_category_featured_item_layout
    }
}