package com.banglalink.toffee.ui.landing

import android.content.res.Resources
import androidx.databinding.ViewDataBinding
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.databinding.ListItemFeaturedPartnerBinding
import com.banglalink.toffee.model.FeaturedPartner
import kotlin.math.ceil

class FeaturedPartnerAdapter(
    val cb: BaseListItemCallback<FeaturedPartner>,
) : BasePagingDataAdapter<FeaturedPartner>(cb, ItemComparator()) {
    
    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_featured_partner
    }
    
    override fun adjustLayout(binding: ViewDataBinding) {
        super.adjustLayout(binding)
        with(binding) {
            if (itemCount > 1 && this is ListItemFeaturedPartnerBinding) {
                val screenWidth = Resources.getSystem().displayMetrics.widthPixels
                val calculatedWidth = screenWidth * 0.7
                featuredPartnerCardView.layoutParams.width = ceil(calculatedWidth).toInt()
            }
        }
    }
    
    override fun onViewRecycled(holder: BaseViewHolder) {
        if (holder.binding is ListItemFeaturedPartnerBinding) {
            holder.binding.partnerBanner.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}