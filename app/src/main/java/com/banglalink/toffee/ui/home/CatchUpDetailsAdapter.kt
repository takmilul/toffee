package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.ui.home.OptionCallBack
import com.banglalink.toffee.model.ChannelInfo
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class CatchUpDetailsAdapter(private val optionCallBack: OptionCallBack, channelCallback:(ChannelInfo)->Unit={}): MyBaseAdapter<ChannelInfo>(channelCallback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        if(position == 0)
            return R.layout.catchup_details_list_header

        if(getItem(position)!!.isLive){
            return R.layout.list_item_live
        }
        return R.layout.list_item_catchup
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bindCallBack(optionCallBack)


    }
}