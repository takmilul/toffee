package com.banglalink.toffee.ui.recent

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.data.database.entities.HistoryItem
import com.banglalink.toffee.model.ChannelInfo

class RecentAdapter(callback: BaseListItemCallback<HistoryItem>):
    BasePagingDataAdapter<HistoryItem>(callback, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        if(getItem(position)?.isLive() == true){
            return R.layout.list_item_recent_live_new
        }
        return R.layout.list_item_recent_catchup_new
    }
}