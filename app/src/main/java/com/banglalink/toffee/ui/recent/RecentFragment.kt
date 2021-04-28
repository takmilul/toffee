package com.banglalink.toffee.ui.recent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.database.entities.HistoryItem
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.handleReport
import com.banglalink.toffee.extension.handleShare
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.report.ReportPopupFragment
import com.banglalink.toffee.ui.widget.MyPopupWindow

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

    override fun onOpenMenu(view: View, item: HistoryItem) {
        super.onOpenMenu(view, item)
        item.channelInfo?.let {
            openMenu(view, item.channelInfo)
        }
    }


    fun openMenu(anchor: View, channelInfo: ChannelInfo) {
        val popupMenu = MyPopupWindow(requireContext(), anchor)
        popupMenu.inflate(R.menu.menu_catchup_item)

        if (channelInfo?.favorite == null || channelInfo.favorite == "0") {
            popupMenu.menu.getItem(0).title = "Add to Favorites"
        } else {
            popupMenu.menu.getItem(0).title = "Remove from Favorites"
        }
        popupMenu.setOnMenuItemClickListener{
            when(it?.itemId){
                R.id.menu_share->{
                    requireActivity().handleShare(channelInfo!!)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav->{
                    requireActivity().handleShare(channelInfo!!)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_report -> {
                    requireActivity().handleReport(channelInfo!!)
                    return@setOnMenuItemClickListener true
                }
                else->{
                    return@setOnMenuItemClickListener false
                }
            }
        }
        popupMenu.show()
    }
}