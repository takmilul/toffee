package com.banglalink.toffee.ui.common

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback

class SingleListAdapter<T: Any>(callback: BaseListItemCallback<T>)
    :MyBaseAdapterV2<T>(callback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_catchup
    }
}