package com.banglalink.toffee.ui.landing

import android.content.res.Resources
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ListItemCategoriesV3Binding
import com.banglalink.toffee.extension.px
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
        else {
            if (binding is ListItemCategoriesV3Binding) {
                val calculatedWidth = (Resources.getSystem().displayMetrics.widthPixels - (16.px * 3)) / 2.5    // 16dp margin
                val calculatedHeight = (calculatedWidth / 16) * 8.21    // Category card ratio = 16:8.21
                val iconSize = calculatedHeight / 2.62  // category_card_height : category_icon_height = 2.62:1
                with(binding) {
                    root.layoutParams.apply {
                        width = calculatedWidth.toInt()
                        height = calculatedHeight.toInt()
                    }
                    categoryCardView.layoutParams.apply {
                        width = calculatedWidth.toInt()
                        height = calculatedHeight.toInt()
                    }
                    icon.maxWidth = iconSize.toInt()
                    icon.maxHeight = iconSize.toInt()
                    if (calculatedWidth < 136.px) {
                        (binding.icon.layoutParams as ViewGroup.MarginLayoutParams).apply {
                            marginStart = 8.px
                            text.textSize = 13f
                        }
                    }
                }
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