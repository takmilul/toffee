package com.banglalink.toffee.ui.category.movie

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.MyBaseAdapterV2

class MoviesAdapter(listener: ProviderIconCallback<ChannelInfo>, private val isSubCategory: Boolean = true) : MyBaseAdapterV2<ChannelInfo>(listener) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return if (isSubCategory) {
            R.layout.list_item_horizontal_content_small
        }
        else {
            R.layout.list_item_horizontal_content_big
        }
    }
}