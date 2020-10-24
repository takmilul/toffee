package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.UgcUserChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.home.UserChannelsListAdapter
import com.banglalink.toffee.ui.useractivities.UserActivitiesMainFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing_user_channels.*
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LandingUserChannelsFragment: HomeBaseFragment() {
    private lateinit var mAdapter: UserChannelsListAdapter

    private val viewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_user_channels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = UserChannelsListAdapter(object: BaseListItemCallback<UgcUserChannelInfo> {
            override fun onItemClicked(item: UgcUserChannelInfo) {
                parentFragment?.findNavController()?.navigate(R.id.action_menu_feed_to_myChannelRatingFragment)
            }
        })

        viewAllButton.setOnClickListener {
            parentFragment?.findNavController()?.
            navigate(R.id.menu_activities,
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
            viewModel.loadUserChannels().collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}