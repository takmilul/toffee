package com.banglalink.toffee.ui.category.drama

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.FragmentDramaSeriesContentBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.home.LandingPageViewModel
import kotlinx.coroutines.flow.collectLatest

class DramaSeriesContentFragment : HomeBaseFragment(), ProviderIconCallback<ChannelInfo> {

    private var category: UgcCategory? = null
    private val viewModel by viewModels<DramaSeriesViewModel>()
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()
    private lateinit var binding: FragmentDramaSeriesContentBinding
    private lateinit var mAdapter: DramaSeriesListAdapter<ChannelInfo>
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_drama_series_content, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        category = parentFragment?.arguments?.getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM) as UgcCategory?
        mAdapter = DramaSeriesListAdapter(this)

        binding.latestVideosList.adapter = mAdapter
        
        observeList(category?.id?.toInt() ?: 0)
        
        /*filterButton.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menu.add("Latest Videos")
            popupMenu.menu.add("Trending Videos")
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { item ->
                latestVideosHeader.text = item.title
                when(item.title){
                    "Latest Videos" -> observeList(category?.id?.toInt() ?: 0)
                    "Trending Videos" -> observeTrendingList(category?.id?.toInt() ?: 0)
                }
                true
            }
        }*/
    }

    private fun observeList(categoryId: Int, subCategoryId: Int = 0) {
        lifecycleScope.launchWhenStarted {
            viewModel.loadDramaSeriesContents.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    override fun onItemClicked(item: ChannelInfo) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }
    
    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOptionClicked(view, item)
        onOptionClicked(view, item)
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        landingPageViewModel.navigateToMyChannel(this, item.id.toInt(), item.channel_owner_id, item.isSubscribed)
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
        
    }
}