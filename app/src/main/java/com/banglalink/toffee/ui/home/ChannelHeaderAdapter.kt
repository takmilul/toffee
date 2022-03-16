package com.banglalink.toffee.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.model.SeriesPlaybackInfo
import com.banglalink.toffee.ui.category.webseries.EpisodeListViewModel
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.SeriesHeaderCallback
import com.suke.widget.SwitchButton

class ChannelHeaderAdapter(
    private val headerData: Any? = null,
    private val cb: ContentReactionCallback<ChannelInfo>? = null,
    private val mPref: SessionPreference,
    private val viewModel: EpisodeListViewModel? = null,
) : RecyclerView.Adapter<ChannelHeaderAdapter.HeaderViewHolder>() {

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
        holder.autoplaySwitchGroup.visibility = View.VISIBLE
        holder.bottomPanelStatus.visibility = View.VISIBLE
        
        when (headerData) {
            is PlaylistPlaybackInfo -> {
                holder.seasonInfoHeader.visibility = View.GONE
                holder.bottomPanelStatus.text = "${headerData.playlistName} (${headerData.playlistItemCount})"
                holder.seasonSpinnerWrap.visibility = View.GONE
                holder.playlistShareButton.isVisible = headerData.isApproved == 1
            }
            is SeriesPlaybackInfo -> {
                holder.seasonInfoHeader.visibility = View.VISIBLE
                holder.seasonInfoHeader.text = "${"S%02d \u2022 E%02d".format(channelInfo?.seasonNo, channelInfo?.episodeNo)}"
                holder.seasonSpinnerWrap.visibility = View.VISIBLE
                holder.bottomPanelStatus.visibility = View.GONE
                holder.seasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long,
                    ) {
                        if (cb is SeriesHeaderCallback) {
                            cb.onSeasonChanged(position + 1)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }
                }
            }
            else -> {
                holder.seasonInfoHeader.visibility = View.GONE
                holder.seasonSpinnerWrap.visibility = View.GONE
                holder.playlistShareButton.visibility =View.GONE
            }
        }

        holder.autoplaySwitch.isChecked = mPref.isAutoplayForRecommendedVideos
        holder.autoplaySwitch.setOnCheckedChangeListener { _, isChecked ->
            mPref.isAutoplayForRecommendedVideos = isChecked
        }
    }

    override fun getItemCount(): Int {
        return if (channelInfo == null) 0 else 1
    }

    fun setChannelInfo(info: ChannelInfo?) {
        channelInfo = info
        notifyDataSetChanged()
    }

    class HeaderViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        val autoplaySwitchGroup: Group = binding.root.findViewById(R.id.autoplay_switch_group)
        val autoplaySwitch: SwitchButton = binding.root.findViewById(R.id.autoPlaySwitch)
        val bottomPanelStatus: TextView = binding.root.findViewById(R.id.bottom_panel_status)
        val seasonInfoHeader: TextView = binding.root.findViewById(R.id.seriesInfo)
        val seasonSpinner: Spinner = binding.root.findViewById(R.id.seasonSpinner)
        val seasonSpinnerWrap: RelativeLayout = binding.root.findViewById(R.id.seasonSpinnerWrap)
        val playlistShareButton: ImageView = binding.root.findViewById(R.id.playlistShareButton)

        fun bind(obj: Any, cb: Any?, pos: Int, vm: EpisodeListViewModel?) {
            binding.setVariable(BR.callback, cb)
            binding.setVariable(BR.position, pos)
            binding.setVariable(BR.data, obj)
            binding.setVariable(BR.viewmodel, vm)
            binding.executePendingBindings()
        }
    }
}