package com.banglalink.toffee.ui.useractivities

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.extension.handleShare
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserActivitiesListFragment : BaseListFragment<UserActivities>(), ProviderIconCallback<UserActivities> {
    
    override val mViewModel by viewModels<UserActivitiesListViewModel>()
    override val mAdapter by lazy { UserActivitiesListAdapter(this) }
    private val homeViewModel by activityViewModels<HomeViewModel>()
    
    companion object {
        fun newInstance(): UserActivitiesListFragment {
            return UserActivitiesListFragment()
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Activities"
        observeChannelDetail()
    }
    
    override fun getEmptyViewInfo(): Triple<Int, String?, String?> {
        return Triple(R.drawable.ic_activities_empty, null, "You don't have any activities yet")
    }
    
    override fun onItemClicked(item: UserActivities) {
        lifecycleScope.launch {
            item.channelInfo?.let {
//                localSync.syncData(it.apply {
//                    it.isFromSportsCategory = it.isVOD && it.categoryId == 16
//                }, true)
                homeViewModel.playContentLiveData.postValue(item.channelInfo)
            }
        }
    }
    
    override fun onProviderIconClicked(item: UserActivities) {
        super.onProviderIconClicked(item)
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channelInfo?.channel_owner_id ?: 0)
    }
    
    private fun observeChannelDetail() {
        observe(mViewModel.myChannelDetail) {
            homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(it.myChannelDetail?.id?.toInt() ?: 0)
        }
    }
    
    override fun onOpenMenu(view: View, item: UserActivities) {
        PopupMenu(requireContext(), view).apply {
            if (item.channelInfo?.isApproved == 1) {
                menu.add(0, R.id.menu_share, 0, "Share")
                menu.add(1, R.id.menu_delete_content, 1, "Delete")
            } else {
                menu.add(0, R.id.menu_delete_content, 0, "Delete")
            }
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_delete_content -> {
                        mViewModel.removeItem(item)
                    }
                    R.id.menu_share -> {
                        item.channelInfo?.let { requireActivity().handleShare(it) }
                    }
                }
                return@setOnMenuItemClickListener true
            }
            show()
        }
    }
}