package com.banglalink.toffee.ui.notification

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.model.Notification
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class NotificationDropdownFragment : SingleListFragmentV2<Notification>(), SingleListItemCallback<Notification> {

    private var enableToolbar: Boolean = false
    
    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"

        @JvmStatic
        fun newInstance(enableToolbar: Boolean): NotificationDropdownFragment {
            val instance = NotificationDropdownFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            instance.arguments = bundle
            return instance
        }
    }
    
    override fun initAdapter() {
        mAdapter = NotificationDropdownAdapter(this)
        mViewModel = ViewModelProvider(this).get(NotificationDropdownViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR) ?: false
        mViewModel.enableToolbar = enableToolbar
    }

    override fun onItemClicked(item: Notification) {
        super.onItemClicked(item)
        val action = NotificationDropdownFragmentDirections.actionNotificationDropdownFragmentToNotificationDetailFragment(item)
        findNavController().navigate(action)
    }
}