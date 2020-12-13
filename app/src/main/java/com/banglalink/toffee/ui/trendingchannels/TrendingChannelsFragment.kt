package com.banglalink.toffee.ui.trendingchannels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.enums.PageType.Channel
import com.banglalink.toffee.ui.home.LandingPageViewModel

class TrendingChannelsFragment : Fragment() {

    private val viewModel by activityViewModels<LandingPageViewModel>()
    
    companion object {
        fun newInstance() = TrendingChannelsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        return inflater.inflate(R.layout.fragment_trending_channels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "Trending Channels"
        viewModel.pageType.value = Channel
        viewModel.categoryId.value = 0
    }
}