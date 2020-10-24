package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ListItemCatchupBinding
import com.banglalink.toffee.model.ChannelInfo
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class CatchUpDetailsAdapter(private val optionCallBack: OptionCallBack, channelCallback:(ChannelInfo)->Unit={}): MyBaseAdapter<ChannelInfo>(channelCallback) {
    override fun getLayoutIdForPosition(position: Int): Int {
//        if(position == 0)
//            return R.layout.catchup_details_list_header_new

        if(values[position].isLive){
            return R.layout.list_item_live
        }
        return R.layout.list_item_catchup
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
//        if(position==0){//setting null for headerview
//            holder.itemView.setOnClickListener(null)
//        }
        holder.bindCallBack(optionCallBack)
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        if(holder.binding is ListItemCatchupBinding){
            holder.binding.poster.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}