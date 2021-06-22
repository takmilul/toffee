package com.banglalink.toffee.ui.landing

import android.content.res.Resources
import androidx.databinding.ViewDataBinding
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.LiveTvItemNewBinding
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.model.ChannelInfo

class ChannelAdapter(cb: BaseListItemCallback<ChannelInfo>):
    BasePagingDataAdapter<ChannelInfo>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.live_tv_item_new
    }
    
    override fun adjustLayout(binding: ViewDataBinding) {
        if (binding is LiveTvItemNewBinding) {
            val calculatedSize = (Resources.getSystem().displayMetrics.widthPixels - (16.px * 5)) / 4.5    // 16dp margin
            binding.icon.layoutParams.apply {
                width = calculatedSize.toInt()
                height = calculatedSize.toInt()
            }
        }
    }
}