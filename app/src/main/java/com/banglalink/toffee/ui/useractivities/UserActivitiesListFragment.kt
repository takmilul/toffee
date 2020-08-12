package com.banglalink.toffee.ui.useractivities

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.CommonSingleListFragment
import com.banglalink.toffee.util.unsafeLazy

class UserActivitiesListFragment: CommonSingleListFragment() {
    companion object {
        fun newInstance(): UserActivitiesListFragment {
            return UserActivitiesListFragment()
        }
    }

    override fun initAdapter() {
        mAdapter = UserActivitiesListAdapter(this) {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }
    }

    private val viewModel by unsafeLazy{
        ViewModelProviders.of(this).get(UserActivitiesListViewModel::class.java)
    }

    override fun loadItems(): LiveData<Resource<List<ChannelInfo>>> {
        return viewModel.loadUserActivities()
    }
}