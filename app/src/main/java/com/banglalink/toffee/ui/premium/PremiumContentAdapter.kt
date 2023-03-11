package com.banglalink.toffee.ui.premium

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.MyBaseAdapter

class PremiumContentAdapter(
    cb: BaseListItemCallback<ChannelInfo>
) : MyBaseAdapter<ChannelInfo>(cb) {
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_premium_content
    }
}