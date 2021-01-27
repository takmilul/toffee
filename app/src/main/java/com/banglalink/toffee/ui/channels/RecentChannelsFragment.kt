package com.banglalink.toffee.ui.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.databinding.FragmentRecentTvChannelsBinding
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import kotlinx.coroutines.flow.collectLatest

class RecentChannelsFragment: BaseFragment() {
    private lateinit var mAdapter: RecentChannelsAdapter
    val viewModel by activityViewModels<AllChannelsViewModel>()
    val homeViewModel by activityViewModels<HomeViewModel>()

    private lateinit var binding: FragmentRecentTvChannelsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecentTvChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = RecentChannelsAdapter(object : BaseListItemCallback<TVChannelItem> {
            override fun onItemClicked(item: TVChannelItem) {
                homeViewModel.fragmentDetailsMutableLiveData.postValue(item.channelInfo)
            }
        })

        with(binding.channelList) {
            adapter = mAdapter
        }

        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            viewModel.loadRecentTvChannels().collectLatest {
                val newList = if(it.isNotEmpty()) it.subList(1, it.size) else it
                if(newList.isEmpty()) {
                    binding.channelTv.visibility = View.GONE
                }
                mAdapter.setItems(newList)
            }
        }
    }
}