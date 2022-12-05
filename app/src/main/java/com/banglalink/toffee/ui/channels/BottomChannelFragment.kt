package com.banglalink.toffee.ui.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.filter
import androidx.paging.map
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentBottomTvChannelsBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.util.BindingUtil
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BottomChannelFragment : BaseFragment() {
    
    private val gson = Gson()
    private var job: Job? = null
    private val binding get() = _binding !!
    @Inject lateinit var bindingUtil: BindingUtil
    private lateinit var mAdapter: BottomChannelAdapter
    val homeViewModel by activityViewModels<HomeViewModel>()
    val viewModel by activityViewModels<AllChannelsViewModel>()
    private var _binding: FragmentBottomTvChannelsBinding? = null
    
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
        
        mAdapter = BottomChannelAdapter(requireContext(), bindingUtil, object : BaseListItemCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                homeViewModel.playContentLiveData.postValue(item)
            }
        })
        
        with(binding.channelList) {
            adapter = mAdapter
            itemAnimator = null
            setHasFixedSize(true)
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
        job?.cancel()
        job = viewLifecycleOwner.lifecycleScope.launch {
            mAdapter.notifyItemRangeRemoved(0, mAdapter.itemCount)
            viewModel.loadAllChannels(isStingray, isFromSportsChannel).collectLatest {
                mAdapter.submitData(it.map {
                    if (it is ChannelInfo) {
                        it
                    } else {
                        gson.fromJson(it as String, ChannelInfo::class.java)
                    }
                }.filter { ! it.isExpired })
            }
        }
    }
}