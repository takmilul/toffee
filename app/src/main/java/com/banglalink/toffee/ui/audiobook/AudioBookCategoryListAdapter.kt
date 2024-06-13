package com.banglalink.toffee.ui.audiobook

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.KabbikItem
import com.banglalink.toffee.ui.common.MyBaseAdapter


class AudioBookCategoryListAdapter(
    cb: BaseListItemCallback<KabbikItem>
) : MyBaseAdapter<KabbikItem>(cb) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_category_audiobook
    }
}
