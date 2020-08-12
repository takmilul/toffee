package com.banglalink.toffee.ui.useractivities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.model.ChannelSubscriptionInfo
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.subscription.PackageListActivity

class ChannelSubscriptionFragment
    :SingleListFragmentV2<ChannelSubscriptionInfo>(), ChannelSubscriptionListItemCallback {

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

    private fun observeChanges() {
        (mViewModel as ChannelSubscriptionViewModel).let { vm->
            vm.itemUpdateEvent.observe(viewLifecycleOwner, Observer {
                mAdapter.reloadItem(it)
            })
        }
    }

    override fun onOpenMenu(item: ChannelSubscriptionInfo) {

    }

    override fun onItemClicked(item: ChannelSubscriptionInfo) {
        Log.e("MENU", "Item clicked - $item")
    }

    override fun onSubscribeClicked(item: ChannelSubscriptionInfo) {
        activity?.launchActivity<PackageListActivity>()
    }

    override fun onNotificationBellClicked(item: ChannelSubscriptionInfo, pos: Int) {
        (mViewModel as ChannelSubscriptionViewModel).toggleNotification(item, pos)
    }
}