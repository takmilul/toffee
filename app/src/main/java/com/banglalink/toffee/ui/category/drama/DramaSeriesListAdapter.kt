package com.banglalink.toffee.ui.category.drama

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.common.paging.ProviderIconCallback

class DramaSeriesListAdapter<T : Any>(
    listener: ProviderIconCallback<T>,
) : BasePagingDataAdapter<T>(listener, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_drama_feed
    }
}