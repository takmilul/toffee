package com.banglalink.toffee.ui.landing

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.MyBaseAdapterV2

class PopularTVChannelsAdapter(cb: BaseListItemCallback<ChannelInfo>):
    MyBaseAdapterV2<ChannelInfo>(cb) {

    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.live_tv_item_new
    }
}