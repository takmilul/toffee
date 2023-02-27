package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.R.drawable
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.databinding.FragmentPremiumBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.MarginItemDecoration

class PremiumFragment : BaseFragment(), BaseListItemCallback<PremiumPack> {
    
    private lateinit var mAdapter: PremiumAdapter
    private var _binding: FragmentPremiumBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<PremiumFragmentArgs>()
    private val viewModel by viewModels<PremiumViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.load(R.drawable.content_loader)
        changeToolbarIcon()
        val contentId = args.contentId
        
        mAdapter = PremiumAdapter(this)
        
        with(binding.premiumPackList) {
            adapter = mAdapter
            addItemDecoration(MarginItemDecoration(12))
//            binding.premContentScroller.post { binding.premContentScroller.fullScroll(View.FOCUS_DOWN) }
        }
        
        observeList()
        viewModel.getPremiumPackList(contentId)
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
                    binding.progressBar.hide()
                    response.data.doIfNotNullOrEmpty {
                        binding.packListHeader.show()
                        binding.premiumPackList.show()
                        mAdapter.addAll(it.toList())
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
        findNavController().navigate(R.id.packDetailsFragment, bundleOf("pack" to item))
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        binding.premiumPackList.adapter = null
        _binding = null
    }
}