package com.banglalink.toffee.ui.userchannel

import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.model.UgcChannelPlaylist
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.common.MyViewHolderV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChannelAddToPlaylistAdapter(callback: SingleListItemCallback<UgcChannelPlaylist>): MyBaseAdapterV2<UgcChannelPlaylist>(callback) {
    
    var selectedPosition = -1
        private set

    override fun onBindViewHolder(holder: MyViewHolderV2, position: Int) {
        super.onBindViewHolder(holder, position)
        val obj = getObjForPosition(position)
        holder.binding.setVariable(BR.selectedPosition, selectedPosition)
        holder.bind(obj, callback, position)
    }
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_playlist_dialog
    }

    fun setSelectedItemPosition(position: Int){
        selectedPosition = position
    }

}