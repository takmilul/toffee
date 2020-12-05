package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.ListItemTrendingNow2Binding
import com.banglalink.toffee.databinding.ListItemTrendingNowBinding

class TrendingNowVideoListAdapter(
    cb: ProviderIconCallback<ChannelInfo>
):BasePagingDataAdapter<ChannelInfo>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return if(position % 2  == 0)
            R.layout.list_item_trending_now
        else
            R.layout.list_item_trending_now2
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        if(holder.binding is ListItemTrendingNowBinding){
            holder.binding.poster.setImageDrawable(null)
        }
        else if (holder.binding is ListItemTrendingNow2Binding){
            holder.binding.poster.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}