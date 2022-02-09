package com.banglalink.toffee.ui.category.webseries

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.common.paging.ProviderIconCallback

class WebSeriesListAdapter<T : Any>(
    listener: ProviderIconCallback<T>,
) : BasePagingDataAdapter<T>(listener, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_drama_feed
    }
}