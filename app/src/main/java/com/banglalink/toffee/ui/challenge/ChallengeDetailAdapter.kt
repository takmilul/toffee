package com.banglalink.toffee.ui.challenge

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.ui.common.MyBaseAdapterV2

class ChallengeDetailAdapter(callback: BaseListItemCallback<String>): MyBaseAdapterV2<String>(callback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_challenge_reward
    }
}