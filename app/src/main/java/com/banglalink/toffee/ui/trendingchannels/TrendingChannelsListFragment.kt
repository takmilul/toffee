package com.banglalink.toffee.ui.trendingchannels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.landing.LandingPopularChannelCallback
import com.banglalink.toffee.ui.landing.UserChannelViewModel
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing_categories.*
import kotlinx.android.synthetic.main.fragment_landing_user_channels.*
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class TrendingChannelsListFragment : HomeBaseFragment() {
    private lateinit var mAdapter: TrendingChannelsListAdapter
    private var categoryInfo: UgcCategory? = null
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()
    private val viewModel by viewModels<TrendingChannelsListViewModel>()
    private val subscriptionViewModel by viewModels<UserChannelViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trending_channels_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryInfo = parentFragment?.arguments?.getParcelable(
            CategoryDetailsFragment.ARG_CATEGORY_ITEM
        )

        mAdapter = TrendingChannelsListAdapter(object : LandingPopularChannelCallback<TrendingChannelInfo> {
            override fun onItemClicked(item: TrendingChannelInfo) {
                landingPageViewModel.navigateToMyChannel(this@TrendingChannelsListFragment, item.id.toInt(), item.channelOwnerId, item.isSubscribed?:0)
            }

            override fun onSubscribeButtonClicked(view: View, info: TrendingChannelInfo) {

                if (info.isSubscribed == 0) {
                    subscriptionViewModel.setSubscriptionStatus(info.id, 1, info.channelOwnerId)
                }
                else {
                    VelBoxAlertDialogBuilder(
                        requireContext(),
                        text = getString(R.string.text_unsubscribe_title),
                        positiveButtonTitle = "Unsubscribe",
                        positiveButtonListener = {
                            subscriptionViewModel.setSubscriptionStatus(info.id, 0, info.channelOwnerId)
                            it?.dismiss()
                        }
                    ).create().show()
                }
            }
        })

        with(userChannelList) {
            layoutManager = GridLayoutManager(context, 3)
            adapter = mAdapter
        }
        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            val content = viewModel.loadUserChannels()
            content.collectLatest {
                mAdapter.submitData(it)
            }
        }

        observe(subscriptionViewModel.subscriptionResponse) {
            if(it is Resource.Success) mAdapter.refresh()
            else requireContext().showToast("Failed to subscribe channel")
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}
