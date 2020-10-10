package com.banglalink.toffee.ui.recent

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo

class RecentAdapter(callback: BaseListItemCallback<ChannelInfo>):
    BasePagingDataAdapter<ChannelInfo>(callback, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        if(getItem(position)?.isLive == true){
            return R.layout.list_item_live
        }
        return R.layout.list_item_catchup
    }
}