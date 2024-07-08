package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.FragmentEditorsChoiceBinding
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showLoadingAnimation
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EditorsChoiceFragment: HomeBaseFragment(), ProviderIconCallback<ChannelInfo> {
    
    private var listJob: Job? = null
    private lateinit var mAdapter: EditorsChoiceListAdapter
    private var _binding: FragmentEditorsChoiceBinding?=null
    private val binding get() = _binding!!
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditorsChoiceBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        binding.editorsChoiceList.adapter = null
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isInitialized = false
        mAdapter = EditorsChoiceListAdapter(this)
        
        with(binding.editorsChoiceList) {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow.collectLatest {
                    val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                    val isEmpty = mAdapter.itemCount <= 0 && ! it.source.refresh.endOfPaginationReached
                    binding.placeholder.isVisible = isEmpty
                    binding.editorsChoiceList.isVisible = ! isEmpty
                    binding.placeholder.showLoadingAnimation(isLoading)
                    isInitialized = true
                    binding.root.visibility = if (isEmpty) View.GONE else View.VISIBLE
                }
            }
            binding.placeholder.isVisible = true
            adapter = mAdapter
        }
        observe(mPref.isViewCountDbUpdatedLiveData) {
            observeList()
        }
        observeList()
    }
    
    private fun observeList() {
        listJob?.cancel()
        listJob = viewLifecycleOwner.lifecycleScope.launch {
            val content = if (landingPageViewModel.pageType.value == PageType.Landing) {
                landingPageViewModel.loadLandingEditorsChoiceContent()
            } else {
                landingPageViewModel.loadEditorsChoiceContent()
            }
            content.collectLatest { 
                mAdapter.submitData(it)
            }
        }
    }

    override fun onItemClicked(item: ChannelInfo) {
        homeViewModel.playContentLiveData.postValue(item)
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
    }
}