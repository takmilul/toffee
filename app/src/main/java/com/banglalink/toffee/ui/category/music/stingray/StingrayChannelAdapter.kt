package com.banglalink.toffee.ui.category.music.stingray

import android.content.res.Resources
import androidx.databinding.ViewDataBinding
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ItemStingrayContentBinding
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.model.ChannelInfo

class StingrayChannelAdapter(cb: BaseListItemCallback<ChannelInfo>):
    BasePagingDataAdapter<ChannelInfo>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_stingray_content
    }
    
    override fun adjustLayout(binding: ViewDataBinding) {
        if (binding is ItemStingrayContentBinding) {
            val calculatedSize = (Resources.getSystem().displayMetrics.widthPixels - (16.px * 5)) / 4.5    // 16dp margin
            binding.icon.layoutParams.apply {
                width = calculatedSize.toInt()
                height = calculatedSize.toInt()
            }
        }
    }
}