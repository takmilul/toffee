package com.banglalink.toffee.ui.fmradio

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.filter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.FmRadioContentResponse
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.databinding.FragmentFmChannelsBinding
import com.banglalink.toffee.databinding.FragmentPremiumChannelsBinding
import com.banglalink.toffee.databinding.PlaceholderStingrayItemBinding
import com.banglalink.toffee.extension.loadPlaceholder
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.showLoadingAnimation
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.category.music.stingray.StingrayViewModel
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.premium.PremiumChannelAdapter
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.widget.GridSpacingItemDecoration
import com.banglalink.toffee.util.BindingUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class FmChannelFragment : HomeBaseFragment(), BaseListItemCallback<ChannelInfo> {

    private lateinit var mAdapter: FmChannelsAdapter
    private var _binding: FragmentFmChannelsBinding? = null
    private val binding get() = _binding!!
    val viewModel by activityViewModels<FmViewModel>()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFmChannelsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var isInitialized = false
        mAdapter = FmChannelsAdapter(this)

        with(binding.fmChannelListview) {
            val calculatedSize = (android.content.res.Resources.getSystem().displayMetrics.widthPixels - (16.px * 5)) / 4.5    // 16dp margin
            this.forEach { placeholderView ->
                val binder = androidx.databinding.DataBindingUtil.bind<PlaceholderStingrayItemBinding>(placeholderView)
                binder?.let {
                    it.icon.layoutParams.apply {
                        width = calculatedSize.toInt()
                        height = calculatedSize.toInt()
                    }
                }
            }
        }

        with(binding.fmChannelListview) {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow.collectLatest {
                    val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                    val isEmpty = mAdapter.itemCount <= 0 && !it.source.refresh.endOfPaginationReached
                    binding.fmChannelListview.isVisible = isEmpty
                    binding.fmChannelListview.isVisible = !isEmpty
                    binding.fmChannelListview.showLoadingAnimation(isLoading)
                    isInitialized = true
                }
            }
            adapter = mAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }
            observeList()
        if (mPref.radioBannerImg.isNotBlank())binding.packBannerImageView.load(mPref.radioBannerImg)
        else binding.packBannerImageView.loadPlaceholder()
    }

    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadStingrayList().collectLatest {
                mAdapter.submitData(it.filter { !it.isExpired })
            }
        }
    }

    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)

        if (item.id.isNotBlank()) {
            homeViewModel.playContentLiveData.postValue(item)
        }
        Log.d("TAG", "onItemClickedItem: "+item.id)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.fmChannelListview.adapter = null
        _binding = null
    }
}