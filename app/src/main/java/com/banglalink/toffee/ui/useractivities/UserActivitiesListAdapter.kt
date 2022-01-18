package com.banglalink.toffee.ui.useractivities

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.databinding.ListItemActivitiesBinding

class UserActivitiesListAdapter(callback: ProviderIconCallback<UserActivities>):
    BasePagingDataAdapter<UserActivities>(callback, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        if(getItem(position)?.channelInfo?.isLinear == true){
            return R.layout.list_item_recent_live_new
        }
        return R.layout.list_item_activities
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        if(holder.binding is ListItemActivitiesBinding) {
            holder.binding.videoThumb.setImageDrawable(null)
            holder.binding.ownerThumb.setImageDrawable(null)
        }
    }
}