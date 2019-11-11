package com.banglalink.toffee.ui.favorite

import com.banglalink.toffee.R
import com.banglalink.toffee.ui.player.ChannelInfo
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter

class FavoriteAdapter : MyBaseAdapter<ChannelInfo>() {
    override fun getLayoutIdForPosition(position: Int): Int {
        if (getItem(position)!!.isLive) {
            return R.layout.list_item_live
        }
        return R.layout.list_item_catchup
    }
}