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
import com.banglalink.toffee.ui.home.CategoriesListAdapter
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.home.UserChannelsListAdapter
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.android.synthetic.main.fragment_landing_categories.*
import kotlinx.android.synthetic.main.fragment_landing_user_channels.*

class LandingUserChannelsFragment: HomeBaseFragment() {
    private lateinit var mAdapter: UserChannelsListAdapter

    val viewModel by unsafeLazy {
        ViewModelProvider(activity!!)[LandingPageViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_user_channels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = UserChannelsListAdapter(this)

//        viewAllButton.setOnClickListener {
//            homeViewModel.viewAllCategories.postValue(true)
//        }

        with(userChannelList) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = mAdapter
            addOnScrollListener(object : EndlessRecyclerViewScrollListener(layoutManager as LinearLayoutManager){
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                    viewModel.loadUserChannels()
                }
            })
        }

        viewModel.loadUserChannels()

        observeList()
    }

    private fun observeList() {
        viewModel.userChannelList.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    Log.e("TTT", "User channel size - ${it.data.size}")
                    mAdapter.addAll(it.data)
                }
                is Resource.Failure -> {
                    Log.e("LOG", it.error.msg)
                }
            }
        })
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}