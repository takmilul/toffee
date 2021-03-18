package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.FragmentEditorsChoiceBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import kotlinx.coroutines.flow.collectLatest

class EditorsChoiceFragment: HomeBaseFragment(), ProviderIconCallback<ChannelInfo> {
    private lateinit var mAdapter: EditorsChoiceListAdapter
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()
    private lateinit var binding: FragmentEditorsChoiceBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditorsChoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = EditorsChoiceListAdapter(this)
        mAdapter.addLoadStateListener {
            if(mAdapter.itemCount > 0) {
                binding.editorsChoiceHeader.visibility = View.VISIBLE
            }
        }

        with(binding.editorsChoiceList) {
            isNestedScrollingEnabled = false
            adapter = mAdapter
        }
        /*val channelInfoList = PagingData.from(listOf(
            ChannelInfo("", program_name = "Program Name", content_provider_name = "Channel", created_at = System.currentTimeMillis().toString()),
            ChannelInfo("", program_name = "Program Name", content_provider_name = "Channel", created_at = System.currentTimeMillis().toString()),
            ChannelInfo("", program_name = "Program Name", content_provider_name = "Channel", created_at = System.currentTimeMillis().toString()),
        ))
        lifecycleScope.launch {
            mAdapter.submitData(channelInfoList)
        }*/
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
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
//        landingPageViewModel.navigateToMyChannel(this, item.id.toInt(), item.channel_owner_id, item.isSubscribed)
    }
}