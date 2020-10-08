package com.banglalink.toffee.ui.notification

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.Notification

class NotificationDropdownFragment : BaseListFragment<Notification>(), BaseListItemCallback<Notification> {

    private var enableToolbar: Boolean = false
    override val mAdapter by lazy { NotificationDropdownAdapter(this) }
    override val mViewModel by viewModels<NotificationDropdownViewModel>()
    
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
    
    /*override fun initAdapter() {
        mAdapter = NotificationDropdownAdapter(this)
        mViewModel = ViewModelProvider(this).get(NotificationDropdownViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR) ?: false
        mViewModel.enableToolbar = enableToolbar
    }*/

    override fun onItemClicked(item: Notification) {
        super.onItemClicked(item)
        val action = NotificationDropdownFragmentDirections.actionNotificationDropdownFragmentToNotificationDetailFragment(item)
        findNavController().navigate(action)
    }

}