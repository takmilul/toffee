package com.banglalink.toffee.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.filter
import androidx.recyclerview.widget.GridLayoutManager
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentPartnersListBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

class PartnersListFragment : BaseFragment(), BaseListItemCallback<ChannelInfo> {
    private lateinit var mAdapter: PartnersListAdapter
    private var _binding: FragmentPartnersListBinding ? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<PartnersViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()
    
    companion object {
        @JvmStatic
        fun newInstance() = PartnersListFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPartnersListBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onDestroyView() {
        binding.listView.adapter = null
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = PartnersListAdapter(this)
        with(binding.listView) {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow
//                    .distinctUntilChangedBy { it.refresh }
                    .collectLatest {
                        val isEmpty = mAdapter.itemCount <= 0 && ! it.source.refresh.endOfPaginationReached
                        binding.contentHeader.isVisible = ! isEmpty
                        binding.listView.isVisible = ! isEmpty
                    }
            }
            layoutManager = GridLayoutManager(context, 3)
            adapter = mAdapter
        }
        observePartners()
    }
    
    private fun observePartners() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getPartnersList.map {
                it.filter { !it.isExpired }
            }.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }
}