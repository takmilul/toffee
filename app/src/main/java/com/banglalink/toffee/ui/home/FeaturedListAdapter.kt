package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class FeaturedListAdapter(private val optionCallBack: OptionCallBack,
                          channelCallback:(ChannelInfo)->Unit={})
    :MyBaseAdapter<ChannelInfo>(channelCallback) {

    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.landing_featured_item_layout
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
//        if(position == 0){//disabling click on header view
//            holder.itemView.setOnClickListener(null)
//        }
        holder.bindCallBack(optionCallBack)
    }
}