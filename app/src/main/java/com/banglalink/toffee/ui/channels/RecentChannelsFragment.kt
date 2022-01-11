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
import com.banglalink.toffee.util.UtilsKt
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RecentChannelsFragment: BaseFragment() {
    private lateinit var mAdapter: RecentChannelsAdapter
    val viewModel by activityViewModels<AllChannelsViewModel>()
    val homeViewModel by activityViewModels<HomeViewModel>()

    private var showSelected = false
    private var isStingray = false

    private var _binding: FragmentRecentTvChannelsBinding ? = null
    private val binding get() = _binding!!

    companion object {
        const val SHOW_SELECTED = "SHOW_SELECTED"
        const val IS_STINGRAY = "is_stingray"

        fun newInstance(showSelected: Boolean, isStingray: Boolean): RecentChannelsFragment {
            val args = Bundle()
            args.putBoolean(SHOW_SELECTED, showSelected)
            args.putBoolean(IS_STINGRAY, isStingray)
            val fragment = RecentChannelsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSelected = arguments?.getBoolean(SHOW_SELECTED, false) ?: false
        isStingray = arguments?.getBoolean(IS_STINGRAY, false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecentTvChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val channelsPadding = resources.getDimension(R.dimen.tv_channels_padding)
        val channelItemWidth = resources.getDimension(R.dimen.channel_width)
        val horizontalGap = (UtilsKt.getScreenWidth() - (channelsPadding * 2) - (3 * channelItemWidth)) / 6

        val recentsMargin = resources.getDimension(R.dimen.recent_channels_margin)
        val leftPadding = horizontalGap - recentsMargin + channelsPadding

        mAdapter = RecentChannelsAdapter(object : BaseListItemCallback<TVChannelItem> {
            override fun onItemClicked(item: TVChannelItem) {
                homeViewModel.playContentLiveData.postValue(item.channelInfo)
            }
        })

        with(binding.channelList) {
            setPadding(leftPadding.toInt(), 0, leftPadding.toInt(), 0)
            adapter = mAdapter
        }

        observeList()
    }

    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadRecentTvChannels(isStingray).map {
                it.filter { it.channelInfo?.isExpired == false }
            }.collectLatest {
                val newList = if(it.isNotEmpty()) {
                    if(showSelected) it.subList(1, it.size) else it.subList(0, it.size - 1)
                } else it
                binding.channelTv.visibility = if(newList.isEmpty()) View.GONE else View.VISIBLE
                mAdapter.setItems(newList)
            }
        }
    }
}