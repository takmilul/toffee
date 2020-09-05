package com.banglalink.toffee.ui.recent

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.CommonSingleListFragment
import com.banglalink.toffee.util.unsafeLazy

class RecentFragment:CommonSingleListFragment() {

    private val viewModel by unsafeLazy{
        ViewModelProviders.of(this).get(RecentViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.listview.clearOnScrollListeners()
        activity?.title = "Recent"
    }

    override fun loadItems(): LiveData<Resource<List<ChannelInfo>>> {
        return viewModel.loadRecentItems()
    }

    override fun handleFavoriteAddedSuccessfully(channelInfo: ChannelInfo) {
        channelInfo.favorite = "1"
        viewModel.updateRecentItem(channelInfo)
    }

    override fun handleFavoriteRemovedSuccessFully(channelInfo: ChannelInfo) {
        channelInfo.favorite = "0"
        viewModel.updateRecentItem(channelInfo)
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
        super.removeItemNotInterestedItem(channelInfo)
        viewModel.deleteRecentItem(channelInfo)
    }



}