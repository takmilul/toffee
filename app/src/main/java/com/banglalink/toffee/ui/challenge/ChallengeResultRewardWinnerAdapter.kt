package com.banglalink.toffee.ui.challenge

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChallengeReward
import com.banglalink.toffee.ui.common.MyBaseAdapterV2

class ChallengeResultRewardWinnerAdapter(callback: BaseListItemCallback<ChallengeReward>): MyBaseAdapterV2<ChallengeReward>(callback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_challenge_winner
    }
}