package com.banglalink.toffee.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.model.SeriesPlaybackInfo
import com.banglalink.toffee.ui.category.drama.EpisodeListViewModel
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.MyViewHolderV2
import com.banglalink.toffee.ui.common.SeriesHeaderCallback
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.android.synthetic.main.list_item_videos.view.*

class ChannelHeaderAdapter(private val headerData: Any? = null,
                           private val cb: ContentReactionCallback<ChannelInfo>? = null,
                           private val viewModel: EpisodeListViewModel? = null
)
    :RecyclerView.Adapter<ChannelHeaderAdapter.HeaderViewHolder>() {

    private var channelInfo: ChannelInfo? = null

    init {
        when (headerData) {
            is ChannelInfo -> channelInfo = headerData
            is PlaylistPlaybackInfo -> channelInfo = headerData.currentItem
            is SeriesPlaybackInfo -> {
                channelInfo = headerData.currentItem
            }
        }
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
            holder.bind(it, cb, position, viewModel)
        }
        when (headerData) {
            is PlaylistPlaybackInfo -> {
                holder.autoplaySwitch.visibility = View.VISIBLE
                holder.bottomPanelStatus.visibility = View.VISIBLE
                holder.seasonInfoHeader.visibility = View.GONE
                holder.bottomPanelStatus.text = "${headerData.playlistName} (${headerData.playlistItemCount})"
            }
            is SeriesPlaybackInfo -> {
                holder.autoplaySwitch.visibility = View.VISIBLE
                holder.seasonInfoHeader.visibility = View.VISIBLE
                holder.seasonInfoHeader.text = "${"S%02d \u2022 E%02d".format(channelInfo?.seasonNo, channelInfo?.episodeNo)}"

                holder.seasonSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if(cb is SeriesHeaderCallback) {
                            cb.onSeasonChanged(position + 1)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }
                }
            }
            else -> {
                holder.autoplaySwitch.visibility = View.GONE
                holder.bottomPanelStatus.visibility = View.GONE
                holder.seasonInfoHeader.visibility = View.GONE
            }
        }
        holder.itemView.findViewById<TextView>(R.id.reactionButton)?.setOnLongClickListener {
            cb?.onReactionLongPressed(it, holder.itemView.reactionCount, channelInfo!!)
            true
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
        val autoplaySwitch: SwitchMaterial = binding.root.findViewById(R.id.autoplay_switch)
        val bottomPanelStatus: TextView = binding.root.findViewById(R.id.bottom_panel_status)
        val seasonInfoHeader: TextView = binding.root.findViewById(R.id.seriesInfo)
        val seasonSpinner: Spinner = binding.root.findViewById(R.id.seasonSpinner)

        fun bind(obj: Any, cb: Any?, pos: Int, vm: EpisodeListViewModel?) {
            binding.setVariable(BR.callback, cb)
            binding.setVariable(BR.position, pos)
            binding.setVariable(BR.data, obj)
            binding.setVariable(BR.viewmodel, vm)
            binding.executePendingBindings()
        }
    }
}