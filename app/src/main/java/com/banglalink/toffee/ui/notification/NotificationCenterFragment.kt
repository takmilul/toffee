package com.banglalink.toffee.ui.notification

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.model.Notification
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class NotificationCenterFragment: SingleListFragmentV2<Notification>(), SingleListItemCallback<Notification> {

    private var enableToolbar: Boolean = false

    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"

        @JvmStatic
        fun newInstance(enableToolbar: Boolean): NotificationCenterFragment {
            val instance = NotificationCenterFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            instance.arguments = bundle
            return instance
        }
    }

    override fun initAdapter() {
        mAdapter = NotificationCenterAdapter(this)
        mViewModel = ViewModelProvider(this).get(NotificationCenterViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR) ?: false
        mViewModel.enableToolbar = enableToolbar
    }
}