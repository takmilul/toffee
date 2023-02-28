package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentPremContentBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel

class PremiumContentFragment : BaseFragment(), BaseListItemCallback<ChannelInfo> {
    
    private lateinit var mAdapter: PremiumContentAdapter
    private var _binding: FragmentPremContentBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremContentBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        mAdapter = PremiumContentAdapter(this)
        
        with(binding.premiumContentListview) {
            adapter = mAdapter
        }
        observeList()
    }
    
    private fun observeList() {
        observe(viewModel.packContentListState) { vodContentList ->
            vodContentList?.let {
                mAdapter.addAll(it)
            }
        }
    }
    
    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        viewModel.selectedPack.value?.let {
            if (it.isPackPurchased) {
                homeViewModel.playContentLiveData.value = item
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        binding.premiumContentListview.adapter = null
        _binding = null
    }
}