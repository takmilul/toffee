package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentLandingTvChannelsBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.ChannelAdapter
import com.banglalink.toffee.ui.home.LandingPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing_tv_channels.*
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class PopularTVChannelsFragment: HomeBaseFragment() {
//    private var isDataLoaded = false
    private lateinit var mAdapter: ChannelAdapter
    private lateinit var binding: FragmentLandingTvChannelsBinding
    val viewModel by activityViewModels<LandingPageViewModel>()
//    private lateinit var channelScrollListener : EndlessRecyclerViewScrollListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLandingTvChannelsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
//        val listLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mAdapter = ChannelAdapter(object : BaseListItemCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
//                if(isDataLoaded) {
                    homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
//                }
            }
        })

//        val channelInfoList = listOf(
//            ChannelInfo(id = "", type = "LIVE"),
//            ChannelInfo(id = "", type = "LIVE"),
//            ChannelInfo(id = "", type = "LIVE"),
//            ChannelInfo(id = "", type = "LIVE"),
//            ChannelInfo(id = "", type = "LIVE"),
//            ChannelInfo(id = "", type = "LIVE"),
//            ChannelInfo(id = "", type = "LIVE"),
//        )
//        mAdapter.removeAll()
//        mAdapter.addAll(channelInfoList)
        
        with(binding.channelList){
//            layoutManager = listLayoutManager
            adapter = mAdapter
        }
        
//        channelScrollListener =  object:EndlessRecyclerViewScrollListener(listLayoutManager){
//            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
//                viewModel.loadChannels()
//            }
//        }
//        binding.channelList.addOnScrollListener(channelScrollListener)

        viewAllButton.setOnClickListener {
            homeViewModel.switchBottomTab.postValue(1)
        }

        observeList()
//        viewModel.loadChannels()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            /*observe(viewModel.channelLiveData){
                when(it){
                    is Success -> {
                        isDataLoaded = true
                        mAdapter.removeAll()
                        mAdapter.addAll(it.data)
                    }
                    is Failure -> {
                        channelScrollListener.resetState()
                        requireActivity().showToast(it.error.msg)
                    }
                }
            }*/
            viewModel.loadChannels.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}