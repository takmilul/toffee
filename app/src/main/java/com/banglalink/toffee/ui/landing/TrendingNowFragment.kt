package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.home.MostPopularVideoListAdapter
import com.banglalink.toffee.ui.home.TrendingNowVideoListAdapter
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.android.synthetic.main.fragment_landing_trending.*
import kotlinx.android.synthetic.main.fragment_most_popular.*

class TrendingNowFragment: HomeBaseFragment() {
    private lateinit var mAdapter: TrendingNowVideoListAdapter

    val viewModel by unsafeLazy {
        ViewModelProvider(activity!!)[LandingPageViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_trending, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = TrendingNowVideoListAdapter(this) {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }

        with(trendingNowList) {
            isNestedScrollingEnabled = false
            adapter = mAdapter
        }

//        viewModel.loadMostPopularVideos()

        observeList()
    }

    private fun observeList() {
        viewModel.trendingNowLiveData.observe(viewLifecycleOwner, Observer {
            mAdapter.removeAll()
            mAdapter.addAll(it)
        })
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}