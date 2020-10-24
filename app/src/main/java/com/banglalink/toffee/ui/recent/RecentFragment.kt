package com.banglalink.toffee.ui.recent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.database.entities.HistoryItem
import com.banglalink.toffee.ui.home.HomeViewModel

class RecentFragment: BaseListFragment<HistoryItem>(), BaseListItemCallback<HistoryItem> {
    override val mAdapter by lazy { RecentAdapter(this) }
    override val mViewModel by viewModels<RecentViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = "Recent"
    }

    override fun onItemClicked(item: HistoryItem) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item.channelInfo)
    }
}