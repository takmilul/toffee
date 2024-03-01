package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.databinding.FragmentPremiumPacksBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.ifNotNullOrEmpty
import com.banglalink.toffee.extension.navigatePopUpTo
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import kotlinx.coroutines.launch

class PremiumPacksFragment : BaseFragment(), BaseListItemCallback<PremiumPack> {
    
    private lateinit var mAdapter: PremiumPackListAdapter
    private var _binding: FragmentPremiumPacksBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private var contentId: String? = null
    private var fromChannelItem: Boolean? = false

    companion object {
        @JvmStatic
        fun newInstance(contentId: String?, clickedFromDrawer: Boolean) = PremiumPacksFragment().apply {
            arguments = Bundle().apply {
                putString("contentId", contentId)
                putBoolean("clickedFromChannelItem", clickedFromDrawer)
            }
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPremiumPacksBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        fromChannelItem = arguments?.getBoolean("clickedFromChannelItem")

        mPref.packSource.value = fromChannelItem
        if (fromChannelItem == true) {
            binding.packListHeader.setText(R.string.prem_content_bundle_title)
            mPref.clickedFromChannelItem.value = fromChannelItem
        } else {
            binding.packListHeader.setText(R.string.premium_pack_list_title)
            mPref.clickedFromChannelItem.value = false
        }
        
        binding.progressBar.load(R.drawable.content_loader)
        
        contentId = arguments?.getString("contentId", "0")
        
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
        observeClick()
        init()
        viewModel.selectedPremiumPack.value = null
    }
    
    private fun init(){
        if (!mPref.isMnpStatusChecked && mPref.isVerifiedUser && mPref.isMnpCallForSubscription){
            observeMnpStatus()
        }
        else{
            viewModel.getPremiumPackList(contentId ?: "0")
            viewModel.clickedOnPackList.value = false
        }
    }
    
    private fun observeClick() {
        observe(viewModel.clickedOnPackList) {
            if (it) {
                viewModel.getPremiumPackList(contentId ?: "0")
            }
        }
    }
    
    private fun observeMnpStatus() {
        observe(homeViewModel.mnpStatusBeanLiveData) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.mnpStatus == 200){
                        mPref.isMnpStatusChecked = true
                    }
                    viewModel.getPremiumPackList(contentId ?: "0")
                }
                is Resource.Failure -> {
                    requireContext().showToast(response.error.msg)
                }
            }
        }
        homeViewModel.getMnpStatus()
    }
    
    private fun observeList() {
        observe(viewModel.packListState) { response ->
            when(response) {
                is Resource.Success -> {
                    binding.progressBar.hide()

                    if (response.data.isEmpty()) {
                        binding.packListHeader.hide()
                        binding.premiumPackList.hide()
                        binding.emptyView.show()
                    }

                    response.data.ifNotNullOrEmpty {

                        mAdapter.removeAll()
                        binding.emptyView.hide()
                        if (response.data.size==1){

                            viewModel.selectedPremiumPack.value = response.data.get(0)
                            ToffeeAnalytics.toffeeLogEvent(
                                ToffeeEvents.PREMIUM_PACK,
                                bundleOf(
                                    "source" to "premium_pack_menu",
                                    "pack_ID" to response.data.get(0).id.toString(),
                                    "pack_name" to response.data.get(0).packTitle
                                )
                            )
                            findNavController().navigatePopUpTo(R.id.packDetailsFragment,
                                popUpTo = R.id.premiumPackListFragment)
//                            findNavController().popBackStack(R.id.packDetailsFragment, true)
                        }else{

                            binding.packListHeader.show()
                            binding.premiumPackList.show()
                            mAdapter.addAll(it.toList())

                        }
                    }
                }
                is Resource.Failure -> {
                    binding.progressBar.hide()
                    requireContext().showToast(response.error.msg)
                }
            }
        }
    }
    
    override fun onItemClicked(item: PremiumPack) {

        Log.d("TAG", "onItemClicked: "+item)
        viewModel.selectedPremiumPack.value = item

        ToffeeAnalytics.toffeeLogEvent(
            ToffeeEvents.PREMIUM_PACK,
            bundleOf(
                "source" to "premium_pack_menu",
                "pack_ID" to item.id.toString(),
                "pack_name" to item.packTitle
            )
        )
        findNavController().navigateTo(R.id.packDetailsFragment)
    }
    
    override fun onPause() {
        super.onPause()
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