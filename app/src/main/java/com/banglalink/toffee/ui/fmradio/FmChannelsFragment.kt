package com.banglalink.toffee.ui.fmradio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.filter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentFmChannelsBinding
import com.banglalink.toffee.extension.showLoadingAnimation
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [FmChannelsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FmChannelsFragment : HomeBaseFragment(), BaseListItemCallback<ChannelInfo> {
    
    private lateinit var mAdapter: FmChannelsAdapter
    private var _binding: FragmentFmChannelsBinding? = null
    private val binding get() = _binding!!
    val viewModel by activityViewModels<FmViewModel>()
    
    companion object {
        fun createInstance() = FmChannelsFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFmChannelsBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isInitialized = false
        mAdapter = FmChannelsAdapter(this)
        
        with(binding.fmChannelListview) {
            viewLifecycleOwner.lifecycleScope.launch {
                mAdapter.loadStateFlow.collectLatest {
                    val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                    val isEmpty = mAdapter.itemCount <= 0 && !it.source.refresh.endOfPaginationReached
                    binding.fmChannelListview.isVisible = isEmpty
                    binding.fmChannelListview.isVisible = !isEmpty
                    binding.fmChannelListview.showLoadingAnimation(isLoading)
                    isInitialized = true
                }
            }
            adapter = mAdapter
            itemAnimator = null
            setHasFixedSize(true)
//            addItemDecoration(GridSpacingItemDecoration(3, 24.px, false))
            layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
        }
        observeList()
    }
    
    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadFmRadioList().collectLatest {
                mAdapter.submitData(it.filter { !it.isExpired })
            }
        }
    }
    
    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        if (item.id.isNotBlank()) {
            homeViewModel.playContentLiveData.postValue(item)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        binding.fmChannelListview.adapter = null
        _binding = null
    }
}