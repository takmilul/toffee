package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ListItemPopularVideosBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelPlaylist
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class MostPopularPlaylistsAdapter(
    cb: BaseListItemCallback<MyChannelPlaylist>
): BasePagingDataAdapter<MyChannelPlaylist>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_most_popular_playlists
    }
}