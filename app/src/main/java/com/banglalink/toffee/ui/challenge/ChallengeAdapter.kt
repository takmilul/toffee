package com.banglalink.toffee.ui.challenge

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.Challenge

class ChallengeAdapter(callback: BaseListItemCallback<Challenge>): BasePagingDataAdapter<Challenge>(callback, ItemComparator()) {
    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_challenges
    }
}