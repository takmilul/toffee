package com.banglalink.toffee.ui.premium

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.ui.common.MyBaseAdapter

class PremiumPackListAdapter(
    cb: BaseListItemCallback<PremiumPack>
) : MyBaseAdapter<PremiumPack>(cb) {
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_premium_pack
    }
}