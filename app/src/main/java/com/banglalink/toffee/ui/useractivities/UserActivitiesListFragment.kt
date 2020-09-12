package com.banglalink.toffee.ui.useractivities

import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class UserActivitiesListFragment: SingleListFragmentV2<ChannelInfo>(),
    SingleListItemCallback<ChannelInfo> {
    companion object {
        fun newInstance(): UserActivitiesListFragment {
            return UserActivitiesListFragment()
        }
    }

    override fun initAdapter() {
        mAdapter = UserActivitiesListAdapter(this)
        mViewModel = ViewModelProvider(this).get(UserActivitiesListViewModel::class.java)
    }

    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_activities_empty, "You don't have any activities yet")
    }

    override fun onItemClicked(item: ChannelInfo) {
//        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }
}