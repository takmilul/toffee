package com.banglalink.toffee.ui.userchannel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.UgcChannelPlaylist
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.common.MyViewHolderV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChannelAddToPlaylistAdapter(private val callback: SingleListItemCallback<UgcChannelPlaylist>): PagingDataAdapter<UgcChannelPlaylist, BaseViewHolder>(ItemComparator()) {
    
    var selectedPosition = -1
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater, viewType, parent, false
        )
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val obj = getItem(position)
        holder.binding.setVariable(BR.selectedPosition, selectedPosition)
        obj?.let {
            holder.bind(obj, callback, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_playlist_dialog
    }
    
    fun getItemByIndex(idx: Int): UgcChannelPlaylist? {
        return getItem(idx)
    }

    fun setSelectedItemPosition(position: Int){
        selectedPosition = position
    }

}