package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelPlaylist
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.ui.category.CategoryDetailsFragmentDirections
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.home.MostPopularPlaylistsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_most_popular.*
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MostPopularPlaylistsFragment: HomeBaseFragment(), ProviderIconCallback<MyChannelPlaylist> {
    private lateinit var mAdapter: MostPopularPlaylistsAdapter

    val viewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_most_popular_playlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = MostPopularPlaylistsAdapter(this)

        with(mostPopularList) {
            adapter = mAdapter
        }

        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            viewModel.loadMostPopularPlaylists().collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    override fun onItemClicked(item: MyChannelPlaylist) {
//                homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
        val isOwner = if(item.channelCreatorId == Preference.getInstance().customerId) 1 else 0
        val action = CategoryDetailsFragmentDirections.actionCategoryDetailsFragmentToMyChannelPlaylistVideosFragment(
            PlaylistPlaybackInfo(item.playlistTableId, item.channelCreatorId, isOwner, item.name, item.totalContent)
        )
        findNavController().navigate(action)
    }

    override fun onProviderIconClicked(item: MyChannelPlaylist) {
        super.onProviderIconClicked(item)
        viewModel.navigateToMyChannel(this, item.channelCreatorId, 0)
    }
    
    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}