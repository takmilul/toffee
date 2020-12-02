package com.banglalink.toffee.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.MyViewHolderV2
import com.google.android.material.switchmaterial.SwitchMaterial

class ChannelHeaderAdapter(private val headerData: Any? = null,
                           private val cb: ContentReactionCallback<ChannelInfo>? = null)
    :RecyclerView.Adapter<ChannelHeaderAdapter.HeaderViewHolder>() {

    private var channelInfo: ChannelInfo? = null

    init {
        if(headerData is ChannelInfo) channelInfo = headerData
        else if(headerData is PlaylistPlaybackInfo) channelInfo = headerData.channelInfo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater, viewType, parent, false
        )
        return HeaderViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.catchup_details_list_header_new
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        channelInfo?.let {
            holder.bind(it, cb, position)
        }
        if(headerData is PlaylistPlaybackInfo) {
            holder.autoplaySwitch.visibility = View.VISIBLE
            holder.bottomPanelStatus.visibility = View.VISIBLE
            holder.bottomPanelStatus.text = "${headerData.playlistName} (${headerData.playlistItemCount})"
        }
    }

    override fun getItemCount(): Int {
        return if(channelInfo == null) 0 else 1
    }

    fun setChannelInfo(info: ChannelInfo?) {
        channelInfo = info
        notifyDataSetChanged()
    }

    class HeaderViewHolder(private val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
        val autoplaySwitch = binding.root.findViewById<SwitchMaterial>(R.id.autoplay_switch)
        val bottomPanelStatus = binding.root.findViewById<TextView>(R.id.bottom_panel_status)

        fun bind(obj: Any, cb: Any?, pos: Int) {
            binding.setVariable(BR.callback, cb)
            binding.setVariable(BR.position, pos)
            binding.setVariable(BR.data, obj)
            binding.executePendingBindings()
        }
    }
}