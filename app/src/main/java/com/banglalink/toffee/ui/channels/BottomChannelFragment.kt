package com.banglalink.toffee.ui.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentBottomTvChannelsBinding
import com.banglalink.toffee.enums.PlayingPage
import com.banglalink.toffee.enums.PlayingPage.FM_RADIO
import com.banglalink.toffee.enums.PlayingPage.SPORTS_CATEGORY
import com.banglalink.toffee.enums.PlayingPage.STINGRAY
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.category.music.stingray.StingrayViewModel
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.fmradio.FmViewModel
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.util.BindingUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BottomChannelFragment : BaseFragment() {
    
    var coroutineScope: CoroutineScope? = null
    @Inject lateinit var bindingUtil: BindingUtil
    private lateinit var mAdapter: BottomChannelAdapter
    private var _binding: FragmentBottomTvChannelsBinding? = null
    private val binding get() = _binding !!
    val homeViewModel by activityViewModels<HomeViewModel>()
    private val fmViewModel by activityViewModels<FmViewModel>()
    val viewModel by activityViewModels<AllChannelsViewModel>()
    private val stingrayViewModel by activityViewModels<StingrayViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBottomTvChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        mAdapter = BottomChannelAdapter(requireContext(), bindingUtil, object : BaseListItemCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                homeViewModel.playContentLiveData.postValue(item)
            }
        })
        
        with(binding.channelList) {
            adapter = mAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }
        
        binding.channelList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when(newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        coroutineScope?.cancel()
                        homeViewModel.isBottomChannelScrolling.value = true
                    }
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        coroutineScope = CoroutineScope(Main)
                        coroutineScope!!.launch {
                            delay(4000)
                            homeViewModel.isBottomChannelScrolling.value = false
                        }
                    }
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    
        observe(viewModel.selectedChannel) {
            mAdapter.setSelectedItem(it)
        }
        
        var lastPlayingPage: PlayingPage? = null
        
        observe(homeViewModel.currentlyPlayingFrom) {
            if (lastPlayingPage != it) {
                when (it) {
                    FM_RADIO -> loadFmRadioChannels()
                    STINGRAY -> loadStingrayChannels()
                    SPORTS_CATEGORY -> loadSportsChannels()
                    else -> loadAllTvChannels()
                }
                lastPlayingPage = it
            }
        }
    }
    
    private fun loadAllTvChannels() {
        android.util.Log.i("Category_", "Calling: allChannels")
        viewLifecycleOwner.lifecycleScope.launch {
            mAdapter.notifyItemRangeRemoved(0, mAdapter.itemCount)
            viewModel.getAllTvChannels().collectLatest { 
                mAdapter.submitData(it)
            }
        }
    }
    
    private fun loadStingrayChannels() {
        android.util.Log.i("Category_", "Calling: stingray")
        viewLifecycleOwner.lifecycleScope.launch {
            mAdapter.notifyItemRangeRemoved(0, mAdapter.itemCount) 
            stingrayViewModel.loadStingrayList().collectLatest { 
                mAdapter.submitData(it)
            }
        }
    }
    
    private fun loadFmRadioChannels() {
        android.util.Log.i("Category_", "Calling: fmRadio")
        viewLifecycleOwner.lifecycleScope.launch {
            mAdapter.notifyItemRangeRemoved(0, mAdapter.itemCount) 
            fmViewModel.loadFmRadioList().collectLatest { 
                mAdapter.submitData(it)
            }
        }
    }
    
    private fun loadSportsChannels() {
        android.util.Log.i("Category_", "Calling: sports channels")
        viewLifecycleOwner.lifecycleScope.launch {
            mAdapter.notifyItemRangeRemoved(0, mAdapter.itemCount)
            viewModel.getSportsChannels().collectLatest { 
                mAdapter.submitData(it)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}