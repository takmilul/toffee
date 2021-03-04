package com.banglalink.toffee.ui.home

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ListItemCategoriesV3Binding
import com.banglalink.toffee.model.Category

class CategoriesListAdapter(
    cb: BaseListItemCallback<Category>,
    private val fullWidth: Boolean = false
): BasePagingDataAdapter<Category>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_categories_v3
    }

    override fun adjustLayout(binding: ViewDataBinding) {
        if(fullWidth) {
            binding.root.layoutParams = binding.root.layoutParams.apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        if(holder.binding is ListItemCategoriesV3Binding){
            holder.binding.icon.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}