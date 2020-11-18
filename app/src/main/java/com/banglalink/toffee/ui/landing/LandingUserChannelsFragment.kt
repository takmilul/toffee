package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.model.UgcUserChannelInfo
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.home.UserChannelsListAdapter
import com.banglalink.toffee.ui.useractivities.UserActivitiesMainFragment
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing_user_channels.*
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LandingUserChannelsFragment : HomeBaseFragment() {
    private lateinit var mAdapter: UserChannelsListAdapter
    private var categoryInfo: UgcCategory? = null
    private val viewModel by activityViewModels<LandingPageViewModel>()
    private val subscriptionViewModel by viewModels<UserChannelViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_user_channels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryInfo = parentFragment?.arguments?.getParcelable(
            CategoryDetailsFragment.ARG_CATEGORY_ITEM
        )

        mAdapter = UserChannelsListAdapter(object : LandingPopularChannelCallback {
            override fun onItemClicked(item: UgcUserChannelInfo) {
                /*val customerId = Preference.getInstance().customerId
                val isOwner = if (item.channelOwnerId == customerId) 1 else 0
                val isPublic = if (item.channelOwnerId == customerId) 0 else 1
                val channelId = item.id.toInt()
                if (parentFragment is CategoryDetailsFragment) {
                    findNavController().navigate(R.id.action_categoryDetailsFragment_to_myChannelHomeFragment, Bundle().apply {
                        putInt(MyChannelHomeFragment.IS_SUBSCRIBED, item.isSubscribed)
                        Log.i("UGC_Home", "onItemClicked: ${item.isSubscribed}")
                        putInt(MyChannelHomeFragment.IS_OWNER, isOwner)
                        putInt(MyChannelHomeFragment.CHANNEL_ID, channelId)
                        putInt(MyChannelHomeFragment.IS_PUBLIC, isPublic)
                        putInt(MyChannelHomeFragment.CHANNEL_OWNER_ID, item.channelOwnerId)
                        putBoolean(MyChannelHomeFragment.IS_FROM_OUTSIDE, true)
                    })
                }
                else {
                    findNavController().navigate(R.id.action_menu_feed_to_myChannelHomeFragment, Bundle().apply {
                        putInt(MyChannelHomeFragment.IS_SUBSCRIBED, item.isSubscribed)
                        Log.i("UGC_Home", "onItemClicked: ${item.isSubscribed}")
                        putInt(MyChannelHomeFragment.IS_OWNER, isOwner)
                        putInt(MyChannelHomeFragment.CHANNEL_ID, channelId)
                        putInt(MyChannelHomeFragment.IS_PUBLIC, isPublic)
                        putInt(MyChannelHomeFragment.CHANNEL_OWNER_ID, item.channelOwnerId)
                        putBoolean(MyChannelHomeFragment.IS_FROM_OUTSIDE, true)
                    })
                }*/
                
                viewModel.navigateToMyChannel(this@LandingUserChannelsFragment, item.channelOwnerId, item.isSubscribed?:0)
            }

            override fun onSubscribeButtonClicked(view: View, info: UgcUserChannelInfo) {

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

        viewAllButton.setOnClickListener {
            parentFragment?.findNavController()?.navigate(R.id.menu_activities,
                Bundle().apply {
                    putInt(UserActivitiesMainFragment.ARG_SELECTED_TAB, 1)
                })
        }

        with(userChannelList) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = mAdapter
        }
        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            val content = if (categoryInfo == null) {
                viewModel.loadUserChannels()
            }
            else {
                viewModel.loadUserChannelsByCategory(categoryInfo!!)
            }
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
