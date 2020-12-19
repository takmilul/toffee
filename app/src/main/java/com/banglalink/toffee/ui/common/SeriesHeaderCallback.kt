package com.banglalink.toffee.ui.common

import com.banglalink.toffee.model.ChannelInfo

interface SeriesHeaderCallback: ContentReactionCallback<ChannelInfo> {
    fun onSeasonChanged(newSeason: Int) {}
}