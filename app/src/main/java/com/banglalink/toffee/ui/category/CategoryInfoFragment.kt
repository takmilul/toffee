package com.banglalink.toffee.ui.category

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.map
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.databinding.FragmentCategoryInfoBinding
import com.banglalink.toffee.extension.handleUrlShare
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.BindingUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CategoryInfoFragment : HomeBaseFragment() {
    
    private val binding get() = _binding!!
    private var category: Category? = null
    @Inject lateinit var localSync: LocalSync
    @Inject lateinit var bindingUtil: BindingUtil
    private var _binding: FragmentCategoryInfoBinding? = null
    private lateinit var mAdapter: CategoryWiseLinearChannelAdapter
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoryInfoBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        category = landingViewModel.selectedCategory.value
        setCategoryUiInfo()
        requireActivity().title = category?.categoryName
        binding.categoryShareButton.safeClick({
            category?.categoryShareUrl?.let { requireActivity().handleUrlShare(it) }
        })
        
        mAdapter = CategoryWiseLinearChannelAdapter(requireContext(), bindingUtil, object : BaseListItemCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                homeViewModel.playContentLiveData.postValue(item.apply {
                    if (isLive && categoryId == 16) {
                        isFromSportsCategory = true
                    }
                })
            }
        })
        
        with(binding.channelList) {
            adapter = mAdapter
            itemAnimator = null
        }
        
        observeLinearList()
        binding.viewAllButton.setOnClickListener {
            findNavController().navigate(R.id.menu_tv)
        }
    }
    
    private fun setCategoryUiInfo() {
        category.let {
            binding.categoryName.text = it?.categoryName
            bindingUtil.bindCategoryIcon(binding.categoryIcon, category)
            binding.categoryIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorAccent2
                )
            )
            binding.channelTv.text = String.format(getString(R.string.category_channels), it?.categoryName)
        }
    }
    
    private fun observeLinearList() {
        viewLifecycleOwner.lifecycleScope.launch {
            landingViewModel.loadCategoryWiseContent(mPref.categoryId.value ?: 0).collectLatest {
                binding.linearGroup.hide()
                binding.nonLinearGroup.show()
                
                mAdapter.submitData(
                    it.map { channel ->
                        localSync.syncData(channel)
                        
                        binding.placeholder.hide()
                        binding.channelList.show()
                        binding.linearGroup.show()
                        binding.nonLinearGroup.hide()
                        channel
                    }
                )
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}