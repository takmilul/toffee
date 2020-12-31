package com.banglalink.toffee.ui.home

import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ListItemVideosBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.ContentReactionCallback

class MostPopularVideoListAdapter(
    private val cb: ContentReactionCallback<ChannelInfo>
): BasePagingDataAdapter<ChannelInfo>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_videos
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.binding.setVariable(BR.isMyChannel, false)
        /*holder.itemView.findViewById<TextView>(R.id.reactionButton)?.setOnLongClickListener {
            cb.onReactionLongPressed(it, holder.itemView.reactionCount, getItem(position)!!)
            true
        }*/
    }
    
    override fun onViewRecycled(holder: BaseViewHolder) {
        if(holder.binding is ListItemVideosBinding){
            holder.binding.poster.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}