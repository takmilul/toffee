package com.banglalink.toffee.ui.category.movie

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.ui.common.MyBaseAdapterV2

class MoviesAdapter<T: Any>(listener: ProviderIconCallback<T>) : MyBaseAdapterV2<T>(listener) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_horizontal_content_small
    }
}