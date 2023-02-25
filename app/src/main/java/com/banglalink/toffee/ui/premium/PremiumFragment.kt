package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.R.drawable
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.databinding.FragmentPremiumBinding
import com.banglalink.toffee.extension.doIfNotNullOrEmpty
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.MarginItemDecoration

class PremiumFragment : BaseFragment(), BaseListItemCallback<PremiumPack> {
    
    private lateinit var mAdapter: PremiumAdapter
    private var _binding: FragmentPremiumBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PremiumViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeToolbarIcon()
    
        mAdapter = PremiumAdapter(this)
        
        with(binding.premiumPackList) {
            adapter = mAdapter
            addItemDecoration(MarginItemDecoration(12))
//            binding.premContentScroller.post { binding.premContentScroller.fullScroll(View.FOCUS_DOWN) }
        }
        
        observeList()
        viewModel.getPremiumPackList()
    }
    
    private fun changeToolbarIcon() {
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setNavigationIcon(drawable.ic_arrow_back)
        toolbar?.setNavigationOnClickListener {
            runCatching {
                findNavController().popBackStack()
            }
        }
    }
    
    private fun observeList() {
        observe(viewModel.premiumPackListState) { response ->
            when(response) {
                is Success -> {
                    response.data.doIfNotNullOrEmpty {
                        mAdapter.addAll(it.toList())
                    }
                }
                is Failure -> {
                    requireContext().showToast(response.error.msg)
                }
            }
        }
    }
    
    override fun onItemClicked(item: PremiumPack) {
        findNavController().navigate(R.id.packDetailsFragment, bundleOf("packId" to item.id))
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        binding.premiumPackList.adapter = null
        _binding = null
    }
}