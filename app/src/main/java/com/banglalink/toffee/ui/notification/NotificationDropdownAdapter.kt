package com.banglalink.toffee.ui.notification

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.data.database.entities.NotificationInfo

class NotificationDropdownAdapter(callback: BaseListItemCallback<NotificationInfo>): BasePagingDataAdapter<NotificationInfo>(callback, ItemComparator()) {
    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_notification
    }
}