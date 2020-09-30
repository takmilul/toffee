package com.banglalink.toffee.ui.useractivities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.subscription.PackageListActivity

class ChannelSubscriptionFragment
    :SingleListFragmentV2<ChannelInfo>(), ChannelSubscriptionListItemCallback {

    companion object {
        fun newInstance(): ChannelSubscriptionFragment {
            return ChannelSubscriptionFragment()
        }
    }

    override fun initAdapter() {
        mAdapter = ChannelSubscriptionListAdapter(this)
        mViewModel = ViewModelProvider(this)[ChannelSubscriptionViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeChanges()
    }

    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_subscriptions_empty, "You haven't subscribed to any channel yet")
    }

    private fun observeChanges() {
        (mViewModel as ChannelSubscriptionViewModel).let { vm->
            vm.itemUpdateEvent.observe(viewLifecycleOwner, Observer {
                mAdapter.reloadItem(it)
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
        (mViewModel as ChannelSubscriptionViewModel).toggleNotification(item, pos)
    }
}