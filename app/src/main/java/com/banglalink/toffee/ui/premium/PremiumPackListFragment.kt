package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.databinding.FragmentPremiumPackListBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import kotlinx.coroutines.launch

class PremiumPackListFragment : BaseFragment(), BaseListItemCallback<PremiumPack> {
    
    private lateinit var mAdapter: PremiumPackListAdapter
    private var _binding: FragmentPremiumPackListBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumPackListBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.load(R.drawable.content_loader)
        onBackIconClicked()
        requireActivity().title = "Premium Packs"
        val contentId = arguments?.getString("contentId")
        
        mAdapter = PremiumPackListAdapter(this)
        val linearLayoutManager = object : LinearLayoutManager(context, VERTICAL, false) {
            override fun onLayoutCompleted(state: RecyclerView.State?) {
                super.onLayoutCompleted(state)
                viewModel.packListScrollState.value?.let {
                    runCatching {
                        binding.premContentScroller.scrollY = it
                    }
                }
            }
        }
        with(binding.premiumPackList) {
            adapter = mAdapter
            layoutManager = linearLayoutManager
            addItemDecoration(MarginItemDecoration(12))
        }
        
        observeList()
        viewModel.selectedPremiumPack.value = null
        viewModel.getPremiumPackList(contentId ?: "0")
    }
    
    private fun observeList() {
        observe(viewModel.packListState) { response ->
            when(response) {
                is Success -> {
                    binding.progressBar.hide()
                    
                    if (response.data.isEmpty()) {
                        binding.packListHeader.hide()
                        binding.premiumPackList.hide()
                        binding.emptyView.show()
                    }
                    
                    response.data.doIfNotNullOrEmpty {
                        binding.packListHeader.show()
                        binding.premiumPackList.show()
                        mAdapter.removeAll()
                        mAdapter.addAll(it.toList())
                        binding.emptyView.hide()
                    }
                }
                is Failure -> {
                    binding.progressBar.hide()
                    requireContext().showToast(response.error.msg)
                }
            }
        }
    }
    
    override fun onItemClicked(item: PremiumPack) {
        viewModel.selectedPremiumPack.value = item
        findNavController().navigateTo(R.id.packDetailsFragment)
    }
    
    private fun onBackIconClicked() {
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            runCatching {
                findNavController().popBackStack()
            }
        }
    }
    
    override fun onStop() {
        super.onStop()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.packListScrollState.value = binding.premContentScroller.scrollY
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        binding.premiumPackList.adapter = null
        _binding = null
    }
}