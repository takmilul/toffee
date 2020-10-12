package com.banglalink.toffee.ui.favorite

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo

class FavoriteAdapter(callback: BaseListItemCallback<ChannelInfo>):
    BasePagingDataAdapter<ChannelInfo>(callback, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        if(getItem(position)?.isLive == true){
            return R.layout.list_item_live_new
        }
        return R.layout.list_item_catchup_new
    }
}