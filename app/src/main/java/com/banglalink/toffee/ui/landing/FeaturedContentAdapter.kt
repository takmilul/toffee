package com.banglalink.toffee.ui.landing

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.MyBaseAdapterV2

class FeaturedContentAdapter(val listener: BaseListItemCallback<ChannelInfo>): MyBaseAdapterV2<ChannelInfo>(listener) {
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.landing_category_featured_item_layout
    }
}