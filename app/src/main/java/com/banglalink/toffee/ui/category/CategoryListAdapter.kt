package com.banglalink.toffee.ui.category

import com.banglalink.toffee.R
import com.banglalink.toffee.model.NavCategory
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class CategoryListAdapter(callback: SingleListItemCallback<NavCategory>): MyBaseAdapterV2<NavCategory>(callback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_categories_new
    }
}