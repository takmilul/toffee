package com.banglalink.toffee.ui.common

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.repository.ContinueWatchingRepository
import com.banglalink.toffee.extension.handleAddToPlaylist
import com.banglalink.toffee.extension.handleFavorite
import com.banglalink.toffee.extension.handleReport
import com.banglalink.toffee.extension.handleShare
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.OptionCallBack
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.category.movie.MovieViewModel
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.MyPopupWindow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
abstract class HomeBaseFragment : BaseFragment(), OptionCallBack {

    @Inject lateinit var favoriteDao: FavoriteItemDao
    @Inject lateinit var contentViewRepo: ContentViewPorgressRepsitory
    @Inject lateinit var continueWatchingRepo: ContinueWatchingRepository
    val homeViewModel by activityViewModels<HomeViewModel>()
    private val movieViewModel by activityViewModels<MovieViewModel>()

    override fun onOptionClicked(anchor: View, channelInfo: ChannelInfo) {
        MyPopupWindow(requireContext(), anchor).apply {
            inflate(R.menu.menu_catchup_item)
            menu.findItem(R.id.menu_fav).isVisible = channelInfo.isApproved == 1
            menu.findItem(R.id.menu_share).isVisible = showShareMenuItem() && channelInfo.isApproved == 1
            menu.findItem(R.id.menu_report).isVisible = mPref.customerId != channelInfo.channel_owner_id
            menu.findItem(R.id.menu_continue_watching)?.isVisible = false

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

    open fun showShareMenuItem(hide: Boolean = false): Boolean {
        return hide
    }
    
    override fun viewAllVideoClick() {
        homeViewModel.viewAllVideoLiveData.postValue(true)
    }
}