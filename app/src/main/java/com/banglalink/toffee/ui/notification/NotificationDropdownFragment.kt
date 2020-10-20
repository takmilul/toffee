package com.banglalink.toffee.ui.notification

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.database.entities.NotificationInfo

class NotificationDropdownFragment : BaseListFragment<NotificationInfo>(), BaseListItemCallback<NotificationInfo> {

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

    override fun onItemClicked(item: NotificationInfo) {
        super.onItemClicked(item)

        if (!item.isSeen) {
            mViewModel.setSeenStatus(item.id!!, true, System.currentTimeMillis())
        }

        val action = NotificationDropdownFragmentDirections.actionNotificationDropdownFragmentToNotificationDetailFragment(item)
        findNavController().navigate(action)
    }

    override fun onOpenMenu(view: View, item: NotificationInfo) {
        super.onOpenMenu(view, item)
        
        PopupMenu(requireContext(), view).apply {
            inflate(R.menu.menu_notification_item)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_unseen_notification -> {
                        mViewModel.setSeenStatus(item.id!!, false, System.currentTimeMillis())
//                        mAdapter.notifyDataSetChanged()
                    }
                    R.id.menu_delete_notification -> {
                        mViewModel.deleteNotification(item)
                    }
                }
                return@setOnMenuItemClickListener true
            }
            show()
        }
    }
}