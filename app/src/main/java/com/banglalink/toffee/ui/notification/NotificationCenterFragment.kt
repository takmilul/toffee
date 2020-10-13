package com.banglalink.toffee.ui.notification

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.database.entities.NotificationInfo

class NotificationCenterFragment: BaseListFragment<NotificationInfo>(), BaseListItemCallback<NotificationInfo> {

    private var enableToolbar: Boolean = false
    override val mAdapter by lazy { NotificationCenterAdapter(this) }
    override val mViewModel by viewModels<NotificationCenterViewModel>()

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

    /*override fun initAdapter() {
        mAdapter = NotificationCenterAdapter(this)
        mViewModel = ViewModelProvider(this).get(NotificationCenterViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR) ?: false
        mViewModel.enableToolbar = enableToolbar
    }*/
}