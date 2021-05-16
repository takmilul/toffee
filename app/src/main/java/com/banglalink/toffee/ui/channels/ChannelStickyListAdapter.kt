package com.banglalink.toffee.ui.channels

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.widget.StickyHeaderGridAdapter
import com.banglalink.toffee.util.bindChannel
import de.hdodenhof.circleimageview.CircleImageView

class ChannelStickyListAdapter(
    private val context: Context,
    private val onItemClickListener: OnItemClickListener?
) :
    StickyHeaderGridAdapter() {
    private var highlightedChannel: ChannelInfo? = null
    private var selectedChannel: ChannelInfo? = null
    private var values: List<StickyHeaderInfo> = emptyList()

    override fun getSectionCount(): Int {
        return values.size + if(selectedChannel == null) 0 else 1
    }

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

    override fun onCreateHeaderViewHolder(
        parent: ViewGroup,
        headerType: Int
    ): StickyHeaderGridAdapter.HeaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            if(headerType == 1) R.layout.catchup_details_list_header_new else R.layout.layout_item_channel_header,
            parent,
            false
        )
        return when(headerType) {
            1 -> {
                TvTitleViewHolder(view)
            }
            else -> {
                HeaderViewHolder(view)
            }
        }
    }

    override fun getSectionHeaderViewType(section: Int): Int {
        if(section == 0 && selectedChannel != null) {
            return 1
        }
        return 2
    }

    override fun onCreateItemViewHolder(
        parent: ViewGroup,
        itemType: Int
    ): ItemViewHolder {
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
        if(viewHolder is TvTitleViewHolder && selectedChannel != null) {
            selectedChannel?.let {
                bindChannel(viewHolder.providerIcon, it)

//                viewHolder.providerIcon.load(it.channel_logo){
//                    transformations(CircleCropTransformation())
////                    crossfade(true)
//                    memoryCachePolicy(CachePolicy.ENABLED)
////            diskCachePolicy(CachePolicy.ENABLED)
//                }

                viewHolder.providerName.text = it.program_name
                viewHolder.nonTvGroup.visibility = View.GONE
            }
        } else {
            val item = getSection(section)
            val headerViewHolder = viewHolder as HeaderViewHolder
            headerViewHolder.text.text = if(item.header == "Recent") "Recently Viewed" else item.header
        }
    }

    override fun onBindItemViewHolder(
        viewHolder: ItemViewHolder,
        section: Int,
        offset: Int
    ) {
        if (onItemClickListener != null) {
            viewHolder.itemView.setOnClickListener { onItemClickListener.onItemClicked(getSection(section).channelInfoList[offset]) }
        }
        val item = getSection(section).channelInfoList[offset]
        val liveTvViewHolder = viewHolder as LiveTvViewHolder

        bindChannel(liveTvViewHolder.icon, item)

//        liveTvViewHolder.icon.load(item.channel_logo){
//            transformations(CircleCropTransformation())
//            crossfade(true)
//            memoryCachePolicy(CachePolicy.ENABLED)
////            diskCachePolicy(CachePolicy.ENABLED)
//        }

        if(item.id == highlightedChannel?.id.toString() && !getSection(section).header.contains("Recent")) {
            liveTvViewHolder.icon.borderWidth = 4.px
        } else {
            liveTvViewHolder.icon.borderWidth = 0
        }

        if(!item.isExpired(SessionPreference.getInstance().getSystemTime())){
            liveTvViewHolder.premimumIcon.visibility = View.INVISIBLE
        }
        else if(item.isPurchased||item.subscription){
            liveTvViewHolder.premimumIcon.visibility = View.INVISIBLE
        }
        else{
            liveTvViewHolder.premimumIcon.visibility = View.VISIBLE
        }

    }

    internal class TvTitleViewHolder(itemView: View):
        StickyHeaderGridAdapter.HeaderViewHolder(itemView) {
        val providerIcon: CircleImageView = itemView.findViewById(R.id.circleImageView)
        val providerName: TextView = itemView.findViewById(R.id.providerName)
        val nonTvGroup: Group = itemView.findViewById(R.id.tvExcludeList)
    }

    internal class HeaderViewHolder(itemView: View) :
        StickyHeaderGridAdapter.HeaderViewHolder(itemView) {
        var text: TextView = itemView.findViewById(R.id.text)

    }

    internal class LiveTvViewHolder(itemView: View) :
        ItemViewHolder(itemView) {
        var icon: CircleImageView = itemView.findViewById(R.id.icon)
        var premimumIcon: ImageView = itemView.findViewById(R.id.premium_icon)

    }

    interface OnItemClickListener {
        fun onItemClicked(channelInfo: ChannelInfo)
    }
}




