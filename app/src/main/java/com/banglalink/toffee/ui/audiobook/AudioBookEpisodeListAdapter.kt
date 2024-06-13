package com.banglalink.toffee.ui.audiobook

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.MyBaseAdapter

class AudioBookEpisodeListAdapter(
    cb: BaseListItemCallback<ChannelInfo>,
) : MyBaseAdapter<ChannelInfo>(cb) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_episode_audiobook
    }
}
