package com.banglalink.toffee.ui.category.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.map
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentLandingTvChannelsBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.landing.ChannelAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class PopularMovieChannelFragment : BaseFragment() {
    private lateinit var mAdapter: ChannelAdapter

    val viewModel by activityViewModels<LandingPageViewModel>()
    val homeViewModel by activityViewModels<HomeViewModel>()
    private var _binding: FragmentLandingTvChannelsBinding ? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandingTvChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.channelTv.text = "Top Movie Channels"

        mAdapter = ChannelAdapter(object : BaseListItemCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
            }
        })

        binding.viewAllButton.setOnClickListener {
//            homeViewModel.switchBottomTab.postValue(1)
            findNavController().navigate(R.id.menu_tv)
        }

        with(binding.channelList) {
            adapter = mAdapter
        }

        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            viewModel.loadPopularMovieChannels.collectLatest {
                val channelList = it.map { tvItem ->
                    tvItem.channelInfo!!
                }
                mAdapter.submitData(channelList)
            }
        }
    }
}
