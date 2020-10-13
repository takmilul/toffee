package com.banglalink.toffee.ui.favorite

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.CommonSingleListFragment
import com.banglalink.toffee.ui.home.HomeViewModel

class FavoriteFragment : BaseListFragment<ChannelInfo>(), BaseListItemCallback<ChannelInfo> {

    override val mAdapter by lazy { FavoriteAdapter(this) }
    override val mViewModel by viewModels<FavoriteViewModel>()

    private val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Favorites"

    }

    override fun onItemClicked(item: ChannelInfo) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }
}