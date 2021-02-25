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
import com.banglalink.toffee.databinding.FragmentRecentTvChannelsBinding
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.UtilsKt
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

        val _10dp = Utils.dpToPx(10)
        val channelItemWidth = resources.getDimension(R.dimen.channel_width)
        val horizontalGap = (UtilsKt.getScreenWidth() - (_10dp * 2) - (3 * channelItemWidth)) / 6

        val recentsMargin = resources.getDimension(R.dimen.recent_channels_margin)
        val leftPadding = horizontalGap - recentsMargin + _10dp

        mAdapter = RecentChannelsAdapter(object : BaseListItemCallback<TVChannelItem> {
            override fun onItemClicked(item: TVChannelItem) {
                homeViewModel.fragmentDetailsMutableLiveData.postValue(item.channelInfo)
            }
        })

        with(binding.channelList) {
            setPadding(leftPadding.toInt(), 0, leftPadding.toInt(), 0)
            adapter = mAdapter
        }

        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            viewModel.loadRecentTvChannels().collectLatest {
                val newList = if(it.isNotEmpty()) it.subList(1, it.size) else it
                binding.channelTv.visibility = if(newList.isEmpty()) View.GONE else View.VISIBLE
                mAdapter.setItems(newList)
            }
        }
    }
}