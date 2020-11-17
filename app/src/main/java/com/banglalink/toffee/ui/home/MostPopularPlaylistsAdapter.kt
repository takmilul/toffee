package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.MyChannelPlaylist
import com.banglalink.toffee.common.paging.ProviderIconCallback

class MostPopularPlaylistsAdapter(
    cb: ProviderIconCallback<MyChannelPlaylist>
): BasePagingDataAdapter<MyChannelPlaylist>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_most_popular_playlists
    }
}