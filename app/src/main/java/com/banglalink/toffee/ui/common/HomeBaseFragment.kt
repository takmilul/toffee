package com.banglalink.toffee.ui.common

import android.view.View
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.extension.handleAddToPlaylist
import com.banglalink.toffee.extension.handleFavorite
import com.banglalink.toffee.extension.handleReport
import com.banglalink.toffee.extension.handleShare
import com.banglalink.toffee.listeners.OptionCallBack
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.MyPopupWindow
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class HomeBaseFragment : BaseFragment(), OptionCallBack {
    
    @Inject lateinit var favoriteDao: FavoriteItemDao
    val homeViewModel by activityViewModels<HomeViewModel>()
    
    override fun onOptionClicked(anchor: View, channelInfo: ChannelInfo) {
        MyPopupWindow(requireContext(), anchor).apply { 
            inflate(R.menu.menu_catchup_item)
            menu.findItem(R.id.menu_fav).isVisible = channelInfo.isApproved == 1
            menu.findItem(R.id.menu_share).isVisible = hideShareMenuItem() && channelInfo.isApproved == 1
            menu.findItem(R.id.menu_report).isVisible = mPref.customerId != channelInfo.channel_owner_id
            
            if (channelInfo.favorite == null || channelInfo.favorite == "0" || !mPref.isVerifiedUser) {
                menu.findItem(R.id.menu_fav).title = "Add to Favorites"
            } else {
                menu.findItem(R.id.menu_fav).title = "Remove from Favorites"
            }
            
            setOnMenuItemClickListener {
                when (it?.itemId) {
                    R.id.menu_share -> {
                        activity?.handleShare(channelInfo)
                    }
                    R.id.menu_fav -> {
                        activity?.handleFavorite(channelInfo, favoriteDao)
                    }
                    R.id.menu_add_to_playlist -> {
                        activity?.handleAddToPlaylist(channelInfo)
                    }
                    R.id.menu_report -> {
                        activity?.handleReport(channelInfo)
                    }
                }
                return@setOnMenuItemClickListener true
            }
        }.show()
    }
    
    open fun hideShareMenuItem(hide: Boolean = false): Boolean {
        return hide
    }
    
    override fun viewAllVideoClick() {
        homeViewModel.viewAllVideoLiveData.postValue(true)
    }
}