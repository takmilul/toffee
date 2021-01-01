package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.ReactionFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.home.MostPopularVideoListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_most_popular.*
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MostPopularFragment: HomeBaseFragment(), ContentReactionCallback<ChannelInfo> {
    private lateinit var mAdapter: MostPopularVideoListAdapter

    val viewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_most_popular, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = MostPopularVideoListAdapter(this)

        with(mostPopularList) {
            adapter = mAdapter
        }

        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            viewModel.loadMostPopularVideos().collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
    override fun onItemClicked(item: ChannelInfo) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }

    override fun onOpenMenu(view: View, item: ChannelInfo) {
        openMenu(view, item)
    }
    
    override fun onReactionClicked(view: View, reactionCountView: View, item: ChannelInfo) {
        super.onReactionClicked(view, reactionCountView, item)
        ReactionFragment.newInstance(item).apply { setView(view, reactionCountView) }.show(requireActivity().supportFragmentManager, ReactionFragment.TAG)
    }

    /*override fun onReactionLongPressed(view: View, reactionCountView: View, item: ChannelInfo) {
        super.onReactionLongPressed(view, reactionCountView, item)
        requireActivity().supportFragmentManager.beginTransaction().add(ReactionFragment.newInstance(view, reactionCountView, item), ReactionFragment.TAG).commit()
    }*/

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        viewModel.navigateToMyChannel(this@MostPopularFragment, item.id.toInt(), item.channel_owner_id, item.isSubscribed)
    }
    
    private fun openMenu(view: View, item: ChannelInfo) {
        super.onOptionClicked(view, item)
    }
}