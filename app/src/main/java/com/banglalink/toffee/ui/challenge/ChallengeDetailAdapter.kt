package com.banglalink.toffee.ui.challenge

import com.banglalink.toffee.R
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChallengeDetailAdapter(callback: SingleListItemCallback<String>): MyBaseAdapterV2<String>(callback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_challenge_reward
    }
}