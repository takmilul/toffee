package com.banglalink.toffee.ui.premium

import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.response.SubsHistoryDetail
import com.banglalink.toffee.ui.common.MyBaseAdapter

class SubscriptionHistoryAdapter(
): MyBaseAdapter<SubsHistoryDetail>(){
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_my_subscription_packs
    }
}