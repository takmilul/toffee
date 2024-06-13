package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentPremiumChannelsBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.GridSpacingItemDecoration

class PremiumChannelFragment : BaseFragment(), BaseListItemCallback<ChannelInfo> {
    
    private lateinit var mAdapter: PremiumChannelAdapter
    private var _binding: FragmentPremiumChannelsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumChannelsBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        mAdapter = PremiumChannelAdapter(this)
        
        with(binding.premiumChannelListview) {
            adapter = mAdapter
            addItemDecoration(GridSpacingItemDecoration(5, 8.px, false))
            layoutManager = GridLayoutManager(requireContext(), 5, RecyclerView.VERTICAL, false)
        }
        observeList()
    }
    
    private fun observeList() {
        observe(viewModel.packChannelListState) { linearChannelList ->
            linearChannelList?.let {
                mAdapter.removeAll()
                mAdapter.addAll(it)
            }
        }
    }
    
    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        viewModel.selectedPremiumPack.value?.let {
            if (it.isPackPurchased) {
                homeViewModel.playContentLiveData.value = item
            }
            else{
                requireContext().showToast(getString(R.string.activate_pack_toast))
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        binding.premiumChannelListview.adapter = null
        _binding = null
    }
}