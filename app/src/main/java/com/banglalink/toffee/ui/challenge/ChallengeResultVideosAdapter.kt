package com.banglalink.toffee.ui.challenge

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.ChannelInfo

class ChallengeResultVideosAdapter(callback: BaseListItemCallback<ChannelInfo>?): BasePagingDataAdapter<ChannelInfo>(callback, ItemComparator()) {
    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_challenge_videos
    }
}