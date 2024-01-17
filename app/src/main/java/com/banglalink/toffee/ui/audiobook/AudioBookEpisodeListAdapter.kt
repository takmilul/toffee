package com.banglalink.toffee.ui.audiobook

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.Episodes
import com.banglalink.toffee.ui.common.MyBaseAdapter

class AudioBookEpisodeListAdapter(
    cb: BaseListItemCallback<Episodes>,
) : MyBaseAdapter<Episodes>(cb) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_episode_audiobook
    }
}
