package com.banglalink.toffee.ui.landing

import android.content.res.Resources
import androidx.databinding.ViewDataBinding
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ListItemLandingUserChannelsBinding
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.model.UserChannelInfo

class LandingUserChannelsListAdapter(cb: BaseListItemCallback<UserChannelInfo>)
    :BasePagingDataAdapter<UserChannelInfo>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_landing_user_channels
    }
    
    override fun adjustLayout(binding: ViewDataBinding) {
        if (binding is ListItemLandingUserChannelsBinding) {
            val calculatedSize = (Resources.getSystem().displayMetrics.widthPixels - (16.px * 4)) / 3.5    // 16dp margin
            with(binding) {
                multiTextButton.setMultitextButtonWidth(calculatedSize.toInt())
                if (calculatedSize < 96.px) {
                    multiTextButton.setMultitextButtonTextSize(11)
                }
                iconHolder.layoutParams.apply {
                    width = calculatedSize.toInt() - 16
                    height = calculatedSize.toInt() - 16
                }
            }
        }
    }
}