package com.banglalink.toffee.ui.subscription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.filter
import androidx.recyclerview.widget.LinearLayoutManager
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.FragmentSubscribedChannelsBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.listeners.LandingPopularChannelCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.UserChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class SubscribedChannelsFragment : HomeBaseFragment() {

    @Inject lateinit var cacheManager: CacheManager
    private lateinit var mAdapter: AllSubscribedChannelAdapter
    private var subscribedChannelInfo: UserChannelInfo? = null
    private var _binding: FragmentSubscribedChannelsBinding ? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SubscribedChannelFragmentViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSubscribedChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = AllSubscribedChannelAdapter(object :
            LandingPopularChannelCallback<UserChannelInfo> {
            override fun onItemClicked(item: UserChannelInfo) {
                homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channelOwnerId)
            }

            override fun onSubscribeButtonClicked(view: View, info: UserChannelInfo) {
                if (info.isSubscribed == 0) {
                    subscribedChannelInfo = info.also {
                        it.isSubscribed = 1
                        it.subscriberCount++
                    }
                    homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, info.channelOwnerId, mPref.customerId) ,1)
                    mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, subscribedChannelInfo)
                    mAdapter.refresh()
                }
                else {
                    UnSubscribeDialog.show(requireContext()) {
                        subscribedChannelInfo = info.also {
                            it.isSubscribed = 0
                            it.subscriberCount--
                        }
                        homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, info.channelOwnerId, mPref.customerId) ,-1)
                        mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, subscribedChannelInfo)
                        mAdapter.refresh()
                    }
                }
            }
        })

        mAdapter.addLoadStateListener {
            mAdapter.apply {
                val showEmpty = itemCount <= 0 && ! it.source.refresh.endOfPaginationReached && it.source.refresh !is LoadState.Loading
                if (showEmpty) {
                    binding.tvSubTv.hide()
                    binding.noChannelTv.show()
                    binding.subscribedChannelList.hide()
                }
                else{
                    binding.tvSubTv.show()
                    binding.noChannelTv.hide()
                    binding.subscribedChannelList.show()
                }
            }
        }

        with(binding.subscribedChannelList) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        observeList()
    }

    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            val content = viewModel.loadUserChannels().map{
                it.filter { item -> item.isSubscribed==1 }
            }
            content.collectLatest {
                mAdapter.submitData(it)
            }
            binding.numberOfSubscription.setText(mAdapter.itemCount)
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
        TODO("Not yet implemented")
    }
}