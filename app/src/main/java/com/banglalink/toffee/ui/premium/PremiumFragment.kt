package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.databinding.FragmentPremiumBinding
import com.banglalink.toffee.extension.doIfNotNullOrEmpty
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import kotlinx.coroutines.launch

class PremiumFragment : BaseFragment(), BaseListItemCallback<PremiumPack> {
    
    private var _binding: FragmentPremiumBinding? = null
    private val binding get() = _binding!!
    private lateinit var mAdapter: PremiumAdapter
    private val viewModel by viewModels<PremiumViewModel>()
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar?.setNavigationOnClickListener {
            runCatching {
                findNavController().popBackStack()
            }
        }
        
        mAdapter = PremiumAdapter(this)
        
        with(binding.premiumBundleList) {
            adapter = mAdapter
            addItemDecoration(MarginItemDecoration(12))
//            binding.premContentScroller.post { binding.premContentScroller.fullScroll(View.FOCUS_DOWN) }
        }
        
        observeList()
        viewModel.getPremiumPackList()
//        (requireActivity() as HomeActivity).binding.tabNavigator.hide()
//        (requireActivity() as HomeActivity).binding.uploadButton.hide()
//        (requireActivity() as HomeActivity).binding.homeBottomSheet.bottomSheet.hide()
//        (requireActivity() as HomeActivity).binding.tbar.toolbar.hide()
//        (requireActivity() as HomeActivity).binding.tbar.toolbarImageView.hide()
    }
    
    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            observe(viewModel.premiumPackListLiveData) {
                when(it) {
                    is Success -> {
                        it.data.doIfNotNullOrEmpty { packList ->
                            mAdapter.addAll(packList.toList())
                        }
                    }
                    is Failure -> {
                        requireContext().showToast(it.error.msg)
                    }
                }
            }
        }
    }
    
    override fun onItemClicked(item: PremiumPack) {
        findNavController().navigate(R.id.packDetailsFragment, bundleOf("packId" to item.id))
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
//        (requireActivity() as HomeActivity).binding.bottomAppBar.show()
//        (requireActivity() as HomeActivity).binding.tabNavigator.show()
//        (requireActivity() as HomeActivity).binding.uploadButton.show()
        
        _binding = null
    }
}