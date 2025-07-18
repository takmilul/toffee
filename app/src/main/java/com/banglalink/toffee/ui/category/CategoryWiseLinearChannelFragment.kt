package com.banglalink.toffee.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.filter
import androidx.paging.map
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.databinding.FragmentCategoryWiseLinearChannelBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.channels.AllChannelsViewModel
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.BindingUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CategoryWiseLinearChannelFragment : BaseFragment() {
    
    private var job: Job? = null
    private val binding get() = _binding!!
    @Inject lateinit var localSync: LocalSync
    @Inject lateinit var bindingUtil: BindingUtil
    val homeViewModel by activityViewModels<HomeViewModel>()
    val viewModel by activityViewModels<LandingPageViewModel>()
    private var _binding: FragmentCategoryWiseLinearChannelBinding? = null
    private lateinit var mAdapter: CategoryWiseLinearChannelAdapter
    private val channelViewModel by activityViewModels<AllChannelsViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoryWiseLinearChannelBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.channelTv.text = (mPref.categoryName.value?: "Sports") + " Channels"
        binding.placeholder.hide()
        binding.channelList.show()
        binding.root.hide()
        mAdapter = CategoryWiseLinearChannelAdapter(requireContext(), bindingUtil, object : BaseListItemCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                homeViewModel.playContentLiveData.postValue(item)
            }
        })
        
        binding.viewAllButton.setOnClickListener {
            findNavController().navigate(R.id.menu_tv)
        }
        
        with(binding.channelList) {
            adapter = mAdapter
            itemAnimator = null
        }
        observeList()
    }
    
    private fun observeList() {
        mPref.isCatWiseLinChannelAvailable.value=false
        job?.cancel()
        job = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadCategoryWiseContent(mPref.categoryId.value ?: 0).collectLatest {
                mAdapter.submitData(it.filter { !it.isExpired }.map { channel ->
                    binding.root.show()
                    mPref.isCatWiseLinChannelAvailable.value = true
//                    localSync.syncData(channel)
                    channel
                })
            }
        }
        channelViewModel.selectedChannel.observe(viewLifecycleOwner) {
            mAdapter.setSelectedItem(it)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}