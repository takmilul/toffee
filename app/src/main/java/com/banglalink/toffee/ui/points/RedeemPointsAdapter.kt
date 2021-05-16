package com.banglalink.toffee.ui.points

import android.app.Activity
import android.view.View
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.ListItemRedeemPointsBinding
import com.banglalink.toffee.model.RedeemPoints
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.common.MyViewHolderV2

class RedeemPointsAdapter(val context: Activity, redeemCallBack: BaseListItemCallback<RedeemPoints>, val getViews: (View, View) -> Unit) : 
MyBaseAdapterV2<RedeemPoints>
    (redeemCallBack) {
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_redeem_points
    }
    
    override fun onBindViewHolder(holder: MyViewHolderV2, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder.binding is ListItemRedeemPointsBinding && position == 0) {
            getViews(holder.binding.view2, holder.binding.textView23)
        }
    }
}