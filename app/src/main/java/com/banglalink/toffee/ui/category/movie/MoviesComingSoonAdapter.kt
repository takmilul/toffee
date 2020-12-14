package com.banglalink.toffee.ui.category.movie

import com.banglalink.toffee.R
import com.banglalink.toffee.model.ComingSoonContent
import com.banglalink.toffee.ui.common.MyBaseAdapterV2

class MoviesComingSoonAdapter: MyBaseAdapterV2<ComingSoonContent>() {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_coming_soon
    }
}