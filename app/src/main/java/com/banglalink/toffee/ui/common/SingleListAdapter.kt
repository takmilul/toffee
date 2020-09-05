package com.banglalink.toffee.ui.common

import com.banglalink.toffee.R

class SingleListAdapter<T: Any>(callback: SingleListItemCallback<T>)
    :MyBaseAdapterV2<T>(callback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_catchup
    }
}