package com.banglalink.toffee.ui.points

import com.banglalink.toffee.R
import com.banglalink.toffee.model.RedeemPoints
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter

class RedeemPointsAdapter(redeemCallBack: (RedeemPoints) -> Unit): MyBaseAdapter<RedeemPoints>(redeemCallBack) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_redeem_points
    }
    
}