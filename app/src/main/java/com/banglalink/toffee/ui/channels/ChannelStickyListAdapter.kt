package com.banglalink.toffee.ui.channels

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
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.widget.StickyHeaderGridAdapter

class ChannelStickyListAdapter(
    private val context: Context,
    private val onItemClickListener: OnItemClickListener?
) :
    StickyHeaderGridAdapter() {

    private var values: List<StickyHeaderInfo> = emptyList()

    override fun getSectionCount(): Int {
        return values.size
    }

    override fun getSectionItemCount(section: Int): Int {
        return values[section].channelInfoList.size
    }

    fun setItems(newList: List<StickyHeaderInfo>) {
        values = newList
        notifyAllSectionsDataSetChanged()
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
            viewHolder.itemView.setOnClickListener { onItemClickListener.onItemClicked(values[section].channelInfoList[offset]) }
        }
        val item = values[section].channelInfoList[offset]
        val liveTvViewHolder = viewHolder as LiveTvViewHolder
        liveTvViewHolder.icon.load(item.channel_logo){
            transformations(CircleCropTransformation())
            crossfade(true)
            memoryCachePolicy(CachePolicy.ENABLED)
//            diskCachePolicy(CachePolicy.ENABLED)
        }

        if(!item.isExpired(Preference.getInstance().getSystemTime())){
            liveTvViewHolder.premimumIcon.visibility = View.INVISIBLE
        }
        else if(item.isPurchased||item.subscription){
            liveTvViewHolder.premimumIcon.visibility = View.INVISIBLE
        }
        else{
            liveTvViewHolder.premimumIcon.visibility = View.VISIBLE
        }

    }


    internal class HeaderViewHolder(itemView: View) :
        StickyHeaderGridAdapter.HeaderViewHolder(itemView) {
        var text: TextView = itemView.findViewById(R.id.text)

    }

    internal class LiveTvViewHolder(itemView: View) :
        ItemViewHolder(itemView) {
        var icon: ImageView = itemView.findViewById(R.id.icon)
        var premimumIcon: ImageView = itemView.findViewById(R.id.premium_icon)

    }

    interface OnItemClickListener {
        fun onItemClicked(channelInfo: ChannelInfo)
    }
}




