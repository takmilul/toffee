package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentPremiumContentsBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.SeriesPlaybackInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.widget.GridSpacingItemDecoration

class PremiumContentFragment : BaseFragment(), BaseListItemCallback<ChannelInfo> {
    
    private lateinit var mAdapter: PremiumContentAdapter
    private var _binding: FragmentPremiumContentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumContentsBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        mAdapter = PremiumContentAdapter(this)
        
        with(binding.premiumContentListview) {
            adapter = mAdapter
            addItemDecoration(GridSpacingItemDecoration(2, 12.px, false))
            layoutManager = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)
        }
        observeList()
    }
    
    private fun observeList() {
        observe(viewModel.packContentListState) { vodContentList ->
            vodContentList?.let {
                mAdapter.removeAll()
                mAdapter.addAll(it)
            }
        }
    }
    
    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        viewModel.selectedPremiumPack.value?.let {
            if (it.isPackPurchased) {
                if (item.seriesSummaryId > 0) {
                    val seriesData = SeriesPlaybackInfo(
                        item.seriesSummaryId,
                        item.seriesName ?: "",
                        item.seasonNo,
                        item.totalSeason,
                        listOf(1),
                        item.video_share_url,
                        item.id.toInt(),
                        item
                    )
                    homeViewModel.addToPlayListMutableLiveData.postValue(
                        AddToPlaylistData(
                            seriesData.playlistId(),
                            listOf(item)
                        )
                    )
                    homeViewModel.playContentLiveData.postValue(seriesData)
                } else {
                    homeViewModel.playContentLiveData.postValue(item)
                }
            }
            else{
                requireContext().showToast(getString(R.string.activate_pack_toast))
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        binding.premiumContentListview.adapter = null
        _binding = null
    }
}