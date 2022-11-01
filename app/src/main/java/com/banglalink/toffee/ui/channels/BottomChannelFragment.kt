package com.banglalink.toffee.ui.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.filter
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.databinding.FragmentBottomTvChannelsBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.util.BindingUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BottomChannelFragment: BaseFragment() {
    @Inject lateinit var bindingUtil: BindingUtil
    private lateinit var mAdapter: BottomChannelAdapter
    val viewModel by activityViewModels<AllChannelsViewModel>()
    val homeViewModel by activityViewModels<HomeViewModel>()
    private var _binding: FragmentBottomTvChannelsBinding ? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBottomTvChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = BottomChannelAdapter(requireContext(), object : BaseListItemCallback<TVChannelItem> {
            override fun onItemClicked(item: TVChannelItem) {
                homeViewModel.playContentLiveData.postValue(item.channelInfo)
            }
        }, bindingUtil)
        
        with(binding.channelList) {
            adapter = mAdapter
        }
        
        observe(viewModel.selectedChannel) {
            mAdapter.setSelectedItem(it)
        }
        observe(homeViewModel.isStingray) {
            observeList(it, false)
        }
        observe(viewModel.isFromSportsCategory) {
            if (it) {
                observeList(homeViewModel.isStingray.value ?: false, it)
            }
        }
    }
    
    private fun observeList(isStingray: Boolean, isFromSportsChannel: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadAllChannels(isStingray, isFromSportsChannel).collectLatest {
                mAdapter.submitData(it.filter { it.channelInfo?.isExpired == false })
            }
        }
    }
}