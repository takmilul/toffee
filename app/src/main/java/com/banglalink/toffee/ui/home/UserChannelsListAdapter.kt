package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.UgcUserChannelInfo
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class UserChannelsListAdapter(cb: BaseListItemCallback<UgcUserChannelInfo>)
    :BasePagingDataAdapter<UgcUserChannelInfo>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_user_channels
    }
}