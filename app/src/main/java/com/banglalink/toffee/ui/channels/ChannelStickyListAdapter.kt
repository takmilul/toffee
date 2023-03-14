package com.banglalink.toffee.ui.channels

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.widget.StickyHeaderGridAdapter
import com.banglalink.toffee.util.BindingUtil

class ChannelStickyListAdapter(
    private val context: Context,
    private val onItemClickListener: OnItemClickListener?,
    private val bindingUtil: BindingUtil
): StickyHeaderGridAdapter() {
    
    private var highlightedChannel: ChannelInfo? = null
    private var selectedChannel: ChannelInfo? = null
    private var values: List<StickyHeaderInfo> = emptyList()
    override val sectionCount: Int
        get() = values.size + if(selectedChannel == null) 0 else 1

    fun setSelected(channel: ChannelInfo?) {
        highlightedChannel = channel
        notifyAllSectionsDataSetChanged()
    }
    
    private fun hasTvHeader(section: Int): Boolean {
        return section == 0 && selectedChannel != null
    }

    private fun getSection(section: Int): StickyHeaderInfo {
        return values[getSectionIndex(section)]
    }

    private fun getSectionIndex(section: Int): Int {
        return if(selectedChannel != null) section - 1 else section
    }

    override fun getSectionItemCount(section: Int): Int {
        if(section == 0 && selectedChannel != null) {
            return 0
        }
        return getSection(section).channelInfoList.size
    }

    fun setItems(newList: List<StickyHeaderInfo>) {
        values = newList
        notifyAllSectionsDataSetChanged()
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup, headerType: Int): StickyHeaderGridAdapter.HeaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            if(headerType == 1) R.layout.catchup_details_list_header_new else R.layout.layout_item_channel_header,
            parent,
            false
        )
        return when(headerType) {
            1 -> TvTitleViewHolder(view)
            else -> HeaderViewHolder(view)
        }
    }

    override fun getSectionHeaderViewType(section: Int): Int {
        if(section == 0 && selectedChannel != null) {
            return 1
        }
        return 2
    }
    
    override fun onCreateItemViewHolder(parent: ViewGroup, itemType: Int): ItemViewHolder {
        return LiveTvViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.live_tv_grid_item, parent, false)
        )
    }

    override fun onBindHeaderViewHolder(viewHolder: StickyHeaderGridAdapter.HeaderViewHolder, section: Int) {
        if(viewHolder is TvTitleViewHolder && selectedChannel != null) {
            selectedChannel?.let {
                bindingUtil.bindChannel(viewHolder.providerIcon, it)
                viewHolder.providerName.text = it.program_name
                viewHolder.nonTvGroup.visibility = View.GONE
            }
        } else {
            val item = getSection(section)
            val headerViewHolder = viewHolder as HeaderViewHolder
            headerViewHolder.text.text = if(item.header == "Recent") "Recently Viewed" else item.header
        }
    }

    override fun onBindItemViewHolder(viewHolder: ItemViewHolder, section: Int, offset: Int) {
        if (onItemClickListener != null) {
            viewHolder.itemView.setOnClickListener { onItemClickListener.onItemClicked(getSection(section).channelInfoList[offset]) }
        }
        val item = getSection(section).channelInfoList[offset]
        val liveTvViewHolder = viewHolder as LiveTvViewHolder
        
        bindingUtil.bindChannel(liveTvViewHolder.icon, item)
//        liveTvViewHolder.premiumIcon.isVisible = item.urlTypeExt == 1
        
        if(item.id == highlightedChannel?.id.toString() && !getSection(section).header.contains("Recent")) {
            liveTvViewHolder.icon.background = ContextCompat.getDrawable(context, R.drawable.selected_channel_bg)
        } else {
            liveTvViewHolder.icon.background = ContextCompat.getDrawable(context, R.drawable.circular_white_bg)
        }
    }
    
    internal class TvTitleViewHolder(itemView: View):
        StickyHeaderGridAdapter.HeaderViewHolder(itemView) {
        val providerIcon: ImageView = itemView.findViewById(R.id.providerImageView)
        val providerName: TextView = itemView.findViewById(R.id.providerName)
        val nonTvGroup: Group = itemView.findViewById(R.id.tvExcludeList)
    }

    internal class HeaderViewHolder(itemView: View) :
        StickyHeaderGridAdapter.HeaderViewHolder(itemView) {
        var text: TextView = itemView.findViewById(R.id.text)
    }

    internal class LiveTvViewHolder(itemView: View) :
        ItemViewHolder(itemView) {
        var icon: ImageView = itemView.findViewById(R.id.icon)
        var premiumIcon: ImageView = itemView.findViewById(R.id.premiumStatusIcon)
    }

    interface OnItemClickListener {
        fun onItemClicked(channelInfo: ChannelInfo)
    }
}




