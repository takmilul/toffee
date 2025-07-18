package com.banglalink.toffee.ui.category.music.stingray

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.filter
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentStingrayBinding
import com.banglalink.toffee.databinding.PlaceholderStingrayItemBinding
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.showLoadingAnimation
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StingrayFragment : HomeBaseFragment(), BaseListItemCallback<ChannelInfo> {
    
    private val binding get() = _binding!!
    private lateinit var mAdapter: StingrayChannelAdapter
    private var _binding: FragmentStingrayBinding? = null
    val viewModel by activityViewModels<StingrayViewModel>()
    
    companion object {
        fun createInstance() = StingrayFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStingrayBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isInitialized = false
        mAdapter = StingrayChannelAdapter(this)
        
        with(binding.placeholder) {
            val calculatedSize = (Resources.getSystem().displayMetrics.widthPixels - (16.px * 5)) / 4.5    // 16dp margin
            this.forEach { placeholderView ->
                val binder = DataBindingUtil.bind<PlaceholderStingrayItemBinding>(placeholderView)
                binder?.let {
                    it.icon.layoutParams.apply {
                        width = calculatedSize.toInt()
                        height = calculatedSize.toInt()
                    }
                }
            }
        }
        
        with(binding.channelList) {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow.collectLatest {
                    val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                    val isEmpty = mAdapter.itemCount <= 0 && !it.source.refresh.endOfPaginationReached
                    binding.placeholder.isVisible = isEmpty
                    binding.channelList.isVisible = !isEmpty
                    binding.placeholder.showLoadingAnimation(isLoading)
                    isInitialized = true
                }
            }
            adapter = mAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }
        if (mPref.isStingrayActive) {
            observeList()
        }
    }
    
    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadStingrayList().collectLatest {
                mAdapter.submitData(it.filter { !it.isExpired })
            }
        }
    }
    
    override fun onItemClicked(item: ChannelInfo) {
        if (item.id.isNotBlank()) {
            homeViewModel.playContentLiveData.postValue(item)
        }
    }
    
    override fun onDestroyView() {
        binding.channelList.adapter = null
        super.onDestroyView()
        _binding = null
    }
}