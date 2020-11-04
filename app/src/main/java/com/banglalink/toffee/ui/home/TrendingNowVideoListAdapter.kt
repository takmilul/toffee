package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ListItemPopularVideosBinding
import com.banglalink.toffee.model.ChannelInfo
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class TrendingNowVideoListAdapter(
    cb: BaseListItemCallback<ChannelInfo>
):BasePagingDataAdapter<ChannelInfo>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return if(position % 2  == 0)
            R.layout.list_item_trending_now
        else
            R.layout.list_item_trending_now2
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        if(holder.binding is ListItemPopularVideosBinding){
            holder.binding.poster.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}