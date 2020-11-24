package com.banglalink.toffee.ui.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.ChannelAdapter
import com.banglalink.toffee.ui.home.HomeViewModel
import kotlinx.android.synthetic.main.fragment_landing_tv_channels.*
import kotlinx.coroutines.flow.collectLatest

class BottomChannelFragment: BaseFragment() {
    private lateinit var mAdapter: BottomChannelAdapter
    val viewModel by activityViewModels<AllChannelsViewModel>()
    val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_tv_channels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = BottomChannelAdapter(object : BaseListItemCallback<TVChannelItem> {
            override fun onItemClicked(item: TVChannelItem) {
                homeViewModel.fragmentDetailsMutableLiveData.postValue(item.channelInfo)
            }
        })

        with(channel_list) {
            adapter = mAdapter
        }

        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            viewModel.loadAllChannels().collectLatest {
                mAdapter.submitData(it)
            }
        }

        observe(viewModel.selectedChannel) {
//            channelAdapter.setSelected(it)
//            detailsAdapter?.setChannelInfo(it)
        }
    }
}