package com.banglalink.toffee.ui.challenge

import com.banglalink.toffee.R
import com.banglalink.toffee.model.Challenge
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChallengeAdapter(callback: SingleListItemCallback<Challenge>): MyBaseAdapterV2<Challenge>(callback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_challenges
    }
}