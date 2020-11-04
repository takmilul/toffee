package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ListItemCategoriesBinding
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.NavCategory
import com.banglalink.toffee.model.UgcCategory
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class CategoriesListAdapter(
    cb: BaseListItemCallback<UgcCategory>
): BasePagingDataAdapter<UgcCategory>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_categories_new
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        if(holder.binding is ListItemCategoriesBinding){
            holder.binding.icon.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}