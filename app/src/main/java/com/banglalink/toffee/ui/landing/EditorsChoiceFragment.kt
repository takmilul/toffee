package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.EditorsChoiceListAdapter
import com.banglalink.toffee.ui.home.LandingPageViewModel
import kotlinx.android.synthetic.main.fragment_editors_choice.*
import kotlinx.coroutines.flow.collectLatest

class EditorsChoiceFragment: HomeBaseFragment(), ProviderIconCallback<ChannelInfo> {
    private lateinit var mAdapter: EditorsChoiceListAdapter
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_editors_choice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = EditorsChoiceListAdapter(this)
        mAdapter.addLoadStateListener {
            if(mAdapter.itemCount > 0) {
                editorsChoiceHeader.visibility = View.VISIBLE
            }
        }

        with(editorsChoiceList) {
            isNestedScrollingEnabled = false
            adapter = mAdapter
        }

        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            landingPageViewModel.loadEditorsChoiceContent().collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }

    override fun onItemClicked(item: ChannelInfo) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        landingPageViewModel.navigateToMyChannel(this, item.id.toInt(), item.channel_owner_id, item.isSubscribed)
    }
}