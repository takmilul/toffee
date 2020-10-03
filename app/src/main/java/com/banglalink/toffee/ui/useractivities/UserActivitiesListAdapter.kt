package com.banglalink.toffee.ui.useractivities

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.TabActivitiesListItemLayout2Binding
import com.banglalink.toffee.model.ChannelInfo

class UserActivitiesListAdapter(callback: BaseListItemCallback<ChannelInfo>):
    BasePagingDataAdapter<ChannelInfo>(callback, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.tab_activities_list_item_layout_2
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        if(holder.binding is TabActivitiesListItemLayout2Binding) {
            holder.binding.videoThumb.setImageDrawable(null)
            holder.binding.ownerThumb.setImageDrawable(null)
        }
    }
}