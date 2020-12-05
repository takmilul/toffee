package com.banglalink.toffee.ui.home

import android.widget.TextView
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ListItemVideosBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.ContentReactionCallback
import kotlinx.android.synthetic.main.list_item_videos.view.*

class PopularVideoListAdapter(
    val cb: ContentReactionCallback<ChannelInfo>
): BasePagingDataAdapter<ChannelInfo>(cb as BaseListItemCallback<ChannelInfo>, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
//        if(position == 0){
//            return R.layout.item_list_header_popular_video
//        }
        return R.layout.list_item_videos
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
//        if(position == 0){//disabling click on header view
//            holder.itemView.setOnClickListener(null)
//        }
        holder.binding.setVariable(BR.isMyChannel, false)
        holder.itemView.findViewById<TextView>(R.id.reactionButton)?.setOnLongClickListener {
            cb.onReactionLongPressed(it, holder.itemView.reactionCount, getItem(position)!!)
            true
        }
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        if(holder.binding is ListItemVideosBinding){
            holder.binding.poster.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }

//    override fun getItemId(position: Int): Long {
//        return values[position].id.hashCode().toLong()
//    }
}