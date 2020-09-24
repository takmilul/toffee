package com.banglalink.toffee.ui.notification

import com.banglalink.toffee.R
import com.banglalink.toffee.model.Notification
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class NotificationDropdownAdapter(callback: SingleListItemCallback<Notification>): MyBaseAdapterV2<Notification>(callback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_notification
    }
}