package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ListItemCategoriesBinding
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.NavCategory
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class CategoriesListAdapter(private val optionCallBack: OptionCallBack, channelCallback:(NavCategory)->Unit={}): MyBaseAdapter<NavCategory>(channelCallback) {

    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_categories
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bindCallBack(optionCallBack)
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        if(holder.binding is ListItemCategoriesBinding){
            holder.binding.icon.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}