package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ListItemCatchupBinding
import com.banglalink.toffee.model.ChannelInfo
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class CatchUpDetailsAdapter(
    cb: BaseListItemCallback<ChannelInfo>
): BasePagingDataAdapter<ChannelInfo>(cb, ItemComparator()) {
    override fun getItemViewType(position: Int): Int {
        if(getItem(position)?.isLive == true){
            return R.layout.list_item_live_new
        }
        return R.layout.list_item_relative
    }
}