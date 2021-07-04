package com.banglalink.toffee.ui.favorite

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.mychannel.MyChannelAddToPlaylistFragment
import com.banglalink.toffee.ui.widget.MyPopupWindow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FavoriteFragment : BaseListFragment<ChannelInfo>(), ProviderIconCallback<ChannelInfo> {

    override val itemMargin: Int = 12
    @Inject lateinit var localSync: LocalSync
    @Inject lateinit var favoriteDao: FavoriteItemDao
    override val verticalPadding = Pair(16, 16)
    override val mAdapter by lazy { FavoriteAdapter(this) }
    override val mViewModel by viewModels<FavoriteViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Favorites"
    }

    override fun onItemClicked(item: ChannelInfo) {
        lifecycleScope.launch {
            localSync.syncData(item)
            homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
        }
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
    }

    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        openMenu(view, item)
    }
    
    fun openMenu(anchor: View, channelInfo: ChannelInfo) {
        val popupMenu = MyPopupWindow(requireContext(), anchor)
        popupMenu.inflate(R.menu.menu_catchup_item)

        if (channelInfo.favorite == null || channelInfo.favorite == "0" || !mPref.isVerifiedUser) {
            popupMenu.menu.getItem(0).title = "Add to Favorites"
        } else {
            popupMenu.menu.getItem(0).title = "Remove from Favorites"
        }
        popupMenu.setOnMenuItemClickListener{
            when(it?.itemId){
                R.id.menu_share->{
                    requireActivity().handleShare(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav->{
                    requireActivity().handleFavorite(channelInfo, favoriteDao, onRemoved = {
                        mAdapter.refresh()
                    })
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_add_to_playlist->{
                    val isUserPlaylist = if (mPref.customerId==channelInfo.channel_owner_id) 0 else 1
                    val fragment = MyChannelAddToPlaylistFragment.newInstance(mPref.customerId, channelInfo, isUserPlaylist)
                    fragment.show(requireActivity().supportFragmentManager, "add_to_playlist")
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_report -> {
                    requireActivity().handleReport(channelInfo)
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