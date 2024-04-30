package com.banglalink.toffee.ui.category.movie

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.handleAddToPlaylist
import com.banglalink.toffee.extension.handleFavorite
import com.banglalink.toffee.extension.handleReport
import com.banglalink.toffee.extension.handleShare
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.widget.MyPopupWindow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MoviesContinueWatchingFragment : MovieBaseFragment<ChannelInfo>() {
    override val cardTitle: String = "Continue Watching"
    
    companion object {
        @JvmStatic
        fun newInstance() = MoviesContinueWatchingFragment()
    }

    override fun onOpenMenu(view: View, item: ChannelInfo) {
        MyPopupWindow(requireContext(), view).apply {
            inflate(R.menu.menu_continue_watching_item)
            menu.findItem(R.id.menu_fav).isVisible = item.isApproved == 1
            menu.findItem(R.id.menu_share).isVisible = showShareMenuItem() && item.isApproved == 1
            menu.findItem(R.id.menu_report).isVisible = mPref.customerId != item.channel_owner_id

            // Conditionally show the "Continue Watching" menu item
            if (cardTitle == "Continue Watching") {
                menu.findItem(R.id.menu_continue_watching)?.isVisible = mPref.isVerifiedUser && item.categoryId == 1
            } else {
                menu.findItem(R.id.menu_continue_watching)?.isVisible = false
            }

            if (item.favorite == null || item.favorite == "0" || !mPref.isVerifiedUser) {
                menu.findItem(R.id.menu_fav).title = "Add to Favorites"
            } else {
                menu.findItem(R.id.menu_fav).title = "Remove from Favorites"
            }

            setOnMenuItemClickListener {
                when (it?.itemId) {
                    R.id.menu_continue_watching -> {
                        lifecycleScope.launch {
                            contentViewRepo.deleteByContentId(mPref.customerId, item.id.toLong())
                            continueWatchingRepo.deleteByContentId(mPref.customerId, item.id.toLong())
                        }
                        requireContext().showToast("Successfully removed from Continue Watching")
                    }
                    R.id.menu_share -> {
                        activity?.handleShare(item)
                    }
                    R.id.menu_fav -> {
                        activity?.handleFavorite(item, favoriteDao)
                    }
                    R.id.menu_add_to_playlist -> {
                        activity?.handleAddToPlaylist(item)
                    }
                    R.id.menu_report -> {
                        activity?.handleReport(item)
                    }
                }
                return@setOnMenuItemClickListener true
            }
        }.show()
    }

    override fun loadContent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getContinueWatchingFlow(1).collectLatest {
                adapter.removeAll()
                adapter.addAll(it)
                if (mPref.isVerifiedUser) showCard(it.isNotEmpty())
            }
        }
    }
}