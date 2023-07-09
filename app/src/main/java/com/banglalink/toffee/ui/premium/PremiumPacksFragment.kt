package com.banglalink.toffee.ui.premium

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.databinding.FragmentPremiumPacksBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.ifNotNullOrEmpty
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.mychannel.MyChannelVideosFragment
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
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
        fun newInstance(clickedFromDrawer: Boolean) = PremiumPacksFragment().apply {
            arguments = Bundle().apply {
                putBoolean("clickedFromChannelItem", clickedFromDrawer)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPremiumPacksBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fromChannelItem = arguments?.getBoolean("clickedFromChannelItem")

        if (fromChannelItem == true) {
            binding.packListHeader.setText(R.string.prem_content_bundle_title)
        } else {
            binding.packListHeader.setText(R.string.premium_pack_list_title)
        }

        binding.progressBar.load(R.drawable.content_loader)
        handleOnBackPressed()

        contentId = arguments?.getString("contentId")

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
        init()
        viewModel.selectedPremiumPack.value = null
    }

    private fun init(){
        if (!mPref.isMnpStatusChecked && mPref.isVerifiedUser && mPref.isMnpCallForSubscription){
            observeMnpStatus()
        }
        else{
            viewModel.getPremiumPackList(contentId ?: "0")
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
                        binding.packListHeader.show()
                        binding.premiumPackList.show()
                        mAdapter.removeAll()
                        mAdapter.addAll(it.toList())
                        binding.emptyView.hide()
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
        viewModel.selectedPremiumPack.value = item
        findNavController().navigateTo(R.id.packDetailsFragment)
    }

    private fun handleOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEnabled) {
                    viewModel.packListScrollState.value = null
                    viewModel.selectedPremiumPack.value = null
                    viewModel.paymentMethod.value = null
                    viewModel.selectedDataPackOption.value = null
                    viewModel.bkashQueryPaymentData.value = null
                    findNavController().popBackStack()
                }
            }
        })
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