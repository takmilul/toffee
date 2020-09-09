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
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.android.synthetic.main.fragment_most_popular.*

class MostPopularFragment: HomeBaseFragment() {
    private lateinit var mAdapter: MostPopularVideoListAdapter

    val viewModel by unsafeLazy {
        ViewModelProvider(activity!!)[LandingPageViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_most_popular, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = MostPopularVideoListAdapter(this) {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }

        with(mostPopularList) {
            adapter = mAdapter

            addOnScrollListener(object : EndlessRecyclerViewScrollListener(layoutManager as LinearLayoutManager){
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                    viewModel.loadMostPopularVideos()
                }
            })
        }

        viewModel.loadMostPopularVideos()

        observeList()
    }

    private fun observeList() {
        viewModel.mostPopularVideoLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    mAdapter.addAll(it.data)
//                    if(it.data.size >= 3){
//                        trendingNowVideoListAdapter.removeAll()
//                        trendingNowVideoListAdapter.addAll(it.data.subList(0,3))
//                        trendingNowVideoListAdapter.notifyDataSetChanged()
//                    }
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