package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ListItemPopularVideosBinding
import com.banglalink.toffee.model.ChannelInfo
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class PopularVideoListAdapter(private val optionCallBack: OptionCallBack, channelCallback:(ChannelInfo)->Unit={}): MyBaseAdapter<ChannelInfo>(channelCallback) {

    override fun getLayoutIdForPosition(position: Int): Int {
        if(position == 0){
            return R.layout.item_list_header_popular_video
        }
        return R.layout.list_item_popular_videos
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if(position == 0){//disabling click on header view
            holder.itemView.setOnClickListener(null)
        }
        holder.bindCallBack(optionCallBack)
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        if(holder.binding is ListItemPopularVideosBinding){
            holder.binding.poster.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}