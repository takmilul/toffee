package com.banglalink.toffee.ui.useractivities

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.subscription.PackageListActivity

class ChannelSubscriptionFragment
    :BaseListFragment<ChannelInfo>(), ChannelSubscriptionListItemCallback {

    override val mAdapter by lazy { ChannelSubscriptionListAdapter(this) }
    override val mViewModel by viewModels<ChannelSubscriptionViewModel>()

    companion object {
        fun newInstance(): ChannelSubscriptionFragment {
            return ChannelSubscriptionFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeChanges()
    }

    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_subscriptions_empty, "You haven't subscribed to any channel yet")
    }

    private fun observeChanges() {
        mViewModel.let { vm->
            vm.itemUpdateEvent.observe(viewLifecycleOwner, Observer {
                mAdapter.notifyItemChanged(it)
            })
        }
    }

    override fun onOpenMenu(anchor: View, item: ChannelInfo) {

    }

    override fun onItemClicked(item: ChannelInfo) {
//        homeViewModel.userChannelMutableLiveData.postValue(item)
        parentFragment?.findNavController()?.navigate(R.id.action_menu_activities_to_channelRatingFragment)
    }

    override fun onSubscribeClicked(item: ChannelInfo) {
        activity?.launchActivity<PackageListActivity>()
    }

    override fun onNotificationBellClicked(item: ChannelInfo, pos: Int) {
        mViewModel.toggleNotification(item, pos)
    }
}