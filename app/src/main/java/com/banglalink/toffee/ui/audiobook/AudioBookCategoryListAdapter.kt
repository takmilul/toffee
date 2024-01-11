package com.banglalink.toffee.ui.audiobook

import android.content.res.Resources
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ListItemCategoriesV3Binding
import com.banglalink.toffee.databinding.ListItemCategoryAudiobookBinding
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo

class AudioBookCategoryListAdapter(
    cb: BaseListItemCallback<ChannelInfo>,
): BasePagingDataAdapter<ChannelInfo>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_category_audiobook
    }
}