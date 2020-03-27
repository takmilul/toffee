package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ListItemPopularVideosBinding
import com.banglalink.toffee.model.ChannelInfo
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class TrendingNowVideoListAdapter(private val optionCallBack: OptionCallBack, channelCallback:(ChannelInfo)->Unit={}): MyBaseAdapter<ChannelInfo>(channelCallback) {

    override fun getLayoutIdForPosition(position: Int): Int {
        return if(position % 2  == 0)
            R.layout.list_item_trending_now
        else
            R.layout.list_item_trending_now2
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bindCallBack(optionCallBack)
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        if(holder.binding is ListItemPopularVideosBinding){
            holder.binding.poster.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}