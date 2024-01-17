package com.banglalink.toffee.ui.audiobook

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.KabbikItemBean
import com.banglalink.toffee.ui.common.MyBaseAdapter


class AudioBookCategoryListAdapter(
    cb: BaseListItemCallback<KabbikItemBean>
) : MyBaseAdapter<KabbikItemBean>(cb) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_category_audiobook
    }
}
