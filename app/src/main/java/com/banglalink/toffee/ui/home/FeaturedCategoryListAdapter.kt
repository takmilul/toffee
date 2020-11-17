package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.MyBaseAdapterV2

class FeaturedCategoryListAdapter(cb: ProviderIconCallback<ChannelInfo>)
    :MyBaseAdapterV2<ChannelInfo>(cb){

    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.landing_category_featured_item_layout
    }
}