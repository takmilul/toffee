package com.banglalink.toffee.ui.channels.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import coil.api.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.banglalink.toffee.R
import com.banglalink.toffee.ui.channels.StickyHeaderInfo
import com.banglalink.toffee.ui.player.ChannelInfo
import com.codewaves.stickyheadergrid.StickyHeaderGridAdapter

class ChannelStickyListAdapter(
    private val context: Context,
    private val values: MutableList<StickyHeaderInfo>,
    private val onItemClickListener: OnItemClickListener?
) :
    StickyHeaderGridAdapter() {

    fun addAll(stickyHeaderInfoList: List<StickyHeaderInfo>) {
        values.addAll(stickyHeaderInfoList)
    }

    override fun getSectionCount(): Int {
        return values.size
    }

    override fun getSectionItemCount(section: Int): Int {
        return values[section].channelInfoList.size
    }

    override fun onCreateHeaderViewHolder(
        parent: ViewGroup,
        headerType: Int
    ): StickyHeaderGridAdapter.HeaderViewHolder {
        return HeaderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_item_channel_header,
                parent,
                false
            )
        )
    }

    override fun onCreateItemViewHolder(
        parent: ViewGroup,
        itemType: Int
    ): StickyHeaderGridAdapter.ItemViewHolder {
        return LiveTvViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.live_tv_grid_item,
                parent,
                false
            )
        )
    }

    override fun onBindHeaderViewHolder(
        viewHolder: StickyHeaderGridAdapter.HeaderViewHolder,
        section: Int
    ) {
        val item = values[section]
        val headerViewHolder = viewHolder as HeaderViewHolder
        headerViewHolder.text.text = item.header
    }

    override fun onBindItemViewHolder(
        viewHolder: ItemViewHolder,
        section: Int,
        offset: Int
    ) {
        if (onItemClickListener != null) {
            viewHolder.itemView.setOnClickListener { v -> onItemClickListener.onItemClicked(values[section].channelInfoList[offset]) }
        }
        val item = values[section].channelInfoList[offset]
        val liveTvViewHolder = viewHolder as LiveTvViewHolder
        liveTvViewHolder.name.text = item.program_name
        liveTvViewHolder.icon.load(item.channel_logo){
            transformations(CircleCropTransformation())
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
        }

    }


    internal class HeaderViewHolder(itemView: View) :
        StickyHeaderGridAdapter.HeaderViewHolder(itemView) {
        var text: TextView

        init {
            text = itemView.findViewById(R.id.text)
        }
    }

    internal class LiveTvViewHolder(itemView: View) :
        StickyHeaderGridAdapter.ItemViewHolder(itemView) {
        var name: TextView
        var icon: ImageView

        init {
            name = itemView.findViewById(R.id.text)
            icon = itemView.findViewById(R.id.icon)
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(channelInfo: ChannelInfo)
    }
}




