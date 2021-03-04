package com.banglalink.toffee.ui.landing

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.UserChannelInfo

class LandingUserChannelsListAdapter(cb: BaseListItemCallback<UserChannelInfo>)
    :BasePagingDataAdapter<UserChannelInfo>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_landing_user_channels
    }
}