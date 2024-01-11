package com.banglalink.toffee.ui.audiobook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.filter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentAudiobookCategoryBinding
import com.banglalink.toffee.databinding.FragmentLandingCategoriesBinding
import com.banglalink.toffee.enums.CategoryType
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showLoadingAnimation
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.fmradio.FmViewModel
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.premium.payment.PaymentStatusDialog
import com.banglalink.toffee.ui.widget.GridSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.checkerframework.checker.units.qual.C

@AndroidEntryPoint
class AudioBookCategoryDetailsFragment: HomeBaseFragment(), BaseListItemCallback<ChannelInfo> {
    
    private val binding get() = _binding!!
    private lateinit var mAdapter: AudioBookCategoryListAdapter
    private var _binding: FragmentAudiobookCategoryBinding ? =null
    val viewModel by activityViewModels<FmViewModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAudiobookCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = arguments?.getString("myTitle", "Kabbik - Audio Book")

        var isInitialized = false

        mAdapter = AudioBookCategoryListAdapter(this)

        _binding?.let { fragmentBinding ->
            with(fragmentBinding.categoriesListAudioBook) {
                viewLifecycleOwner.lifecycleScope.launch {
                    mAdapter.loadStateFlow.collectLatest {
                        val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                        val isEmpty = mAdapter.itemCount <= 0 && !it.source.refresh.endOfPaginationReached
                        fragmentBinding.categoriesListAudioBook.isVisible = isEmpty
                        fragmentBinding.categoriesListAudioBook.isVisible = !isEmpty
                        fragmentBinding.categoriesListAudioBook.showLoadingAnimation(isLoading)
                        isInitialized = true
                    }
                }
                adapter = mAdapter
                itemAnimator = null
                setHasFixedSize(true)
//            addItemDecoration(GridSpacingItemDecoration(3, 24.px, false))
                layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
            }
        }

        observeList()
    }

    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadFmRadioList().collectLatest {
                mAdapter.submitData(it.filter { !it.isExpired })
            }
        }
    }

    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        if (item.id.isNotBlank()) {
            homeViewModel.playContentLiveData.postValue(item)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}