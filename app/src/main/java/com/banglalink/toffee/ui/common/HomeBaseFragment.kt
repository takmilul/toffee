package com.banglalink.toffee.ui.common

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.listeners.OptionCallBack
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.mychannel.MyChannelAddToPlaylistFragment
import com.banglalink.toffee.ui.widget.MyPopupWindow
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class HomeBaseFragment:BaseFragment(), OptionCallBack {

    @Inject
    lateinit var favoriteDao: FavoriteItemDao
    val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onOptionClicked(anchor: View, channelInfo: ChannelInfo) {
        val popupMenu = MyPopupWindow(requireContext(), anchor)
        popupMenu.inflate(R.menu.menu_catchup_item)

        if (channelInfo.favorite == null || channelInfo.favorite == "0" || !mPref.isVerifiedUser) {
            popupMenu.menu.getItem(0).title = "Add to Favorites"
        } else {
            popupMenu.menu.getItem(0).title = "Remove from Favorites"
        }

        popupMenu.menu.findItem(R.id.menu_share).isVisible = hideShareMenuItem() && channelInfo.isApproved == 1
        popupMenu.menu.findItem(R.id.menu_report).isVisible = mPref.customerId != channelInfo.channel_owner_id
        popupMenu.setOnMenuItemClickListener{
            when(it?.itemId){
                R.id.menu_share->{
                    requireActivity().handleShare(channelInfo)
                }
                R.id.menu_fav->{
                    requireActivity().handleFavorite(channelInfo, favoriteDao)
                }
                R.id.menu_add_to_playlist->{
                    val isUserPlaylist = if (mPref.customerId==channelInfo.channel_owner_id) 0 else 1
                    val fragment = MyChannelAddToPlaylistFragment.newInstance(mPref.customerId, channelInfo, isUserPlaylist)
                    fragment.show(requireActivity().supportFragmentManager, "add_to_playlist")
                }
                R.id.menu_report -> {
                    requireActivity().handleReport(channelInfo)
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }

    open fun hideShareMenuItem(hide: Boolean = false): Boolean {
        return hide
    }

    override fun viewAllVideoClick() {
        homeViewModel.viewAllVideoLiveData.postValue(true)
    }
}