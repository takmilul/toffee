package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.ChannelAdapter
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.android.synthetic.main.fragment_landing_tv_channels.*

class PopularTVChannelsFragment: HomeBaseFragment() {
    private lateinit var mAdapter: ChannelAdapter

    val viewModel by unsafeLazy {
        ViewModelProvider(activity!!)[LandingPageViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_tv_channels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = ChannelAdapter {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }

        viewAllButton.setOnClickListener {
            homeViewModel.viewAllChannelLiveData.postValue(true)
        }

        with(channel_list) {
            adapter = mAdapter

            addOnScrollListener(object : EndlessRecyclerViewScrollListener(layoutManager as LinearLayoutManager){
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                    viewModel.loadChannels()
                }
            })
        }

        viewModel.loadChannels()

        observeList()
    }

    private fun observeList() {
        viewModel.channelLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    mAdapter.addAll(it.data)
                }
                is Resource.Failure -> {
//                    channelScrollListener.resetState()
                    requireActivity().showToast(it.error.msg)
                }
            }
        })
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}