package com.banglalink.toffee.ui.useractivities

import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.TabActivitiesListItemLayoutBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.common.MyViewHolderV2
import com.banglalink.toffee.ui.common.SingleListItemCallback
import com.banglalink.toffee.ui.home.OptionCallBack
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class UserActivitiesListAdapter(callback: SingleListItemCallback<ChannelInfo>):
    MyBaseAdapterV2<ChannelInfo>(callback) {
    override fun getLayoutIdForPosition(position: Int): Int {
//        if(getItem(position)!!.isLive){
//            return R.layout.tab_activities_live_item_layout
//        }
        return R.layout.tab_activities_list_item_layout
    }

    override fun onViewRecycled(holder: MyViewHolderV2) {
        super.onViewRecycled(holder)
        if(holder.binding is TabActivitiesListItemLayoutBinding) {
            holder.binding.videoThumb.setImageDrawable(null)
            holder.binding.ownerThumb.setImageDrawable(null)
        }
    }
}