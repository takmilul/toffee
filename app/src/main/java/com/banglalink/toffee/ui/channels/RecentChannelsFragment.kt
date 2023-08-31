package com.banglalink.toffee.ui.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.databinding.FragmentRecentTvChannelsBinding
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.util.Utils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RecentChannelsFragment : BaseFragment() {
    private var isStingray = false
    private var isFmRadio = false
    private var showSelected = false
    private lateinit var mAdapter: RecentChannelsAdapter
    private var _binding: FragmentRecentTvChannelsBinding? = null
    private val binding get() = _binding!!
    val homeViewModel by activityViewModels<HomeViewModel>()
    val viewModel by activityViewModels<AllChannelsViewModel>()
    
    companion object {
        const val SHOW_SELECTED = "SHOW_SELECTED"
        const val IS_STINGRAY = "is_stingray"
        const val IS_FM_RADIO = "isFmRadio"

        fun newInstance(showSelected: Boolean, isStingray: Boolean, isFmRadio: Boolean): RecentChannelsFragment {
            val args = Bundle()
            args.putBoolean(SHOW_SELECTED, showSelected)
            args.putBoolean(IS_STINGRAY, isStingray)
            args.putBoolean(IS_FM_RADIO, isFmRadio)
            val fragment = RecentChannelsFragment()
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSelected = arguments?.getBoolean(SHOW_SELECTED, false) ?: false
        isStingray = arguments?.getBoolean(IS_STINGRAY, false) ?: false
        isFmRadio = arguments?.getBoolean(IS_FM_RADIO, false) ?: false
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRecentTvChannelsBinding.inflate(inflater, container, false)
        return _binding?.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val channelsPadding = resources.getDimension(R.dimen.tv_channels_padding)
        val channelItemWidth = resources.getDimension(R.dimen.channel_width)
        val horizontalGap = (Utils.getScreenWidth() - (channelsPadding * 2) - (3 * channelItemWidth)) / 6
        
        val recentMargin = resources.getDimension(R.dimen.recent_channels_margin)
        val leftPadding = horizontalGap - recentMargin + channelsPadding
        
        mAdapter = RecentChannelsAdapter(object : BaseListItemCallback<TVChannelItem> {
            override fun onItemClicked(item: TVChannelItem) {
                homeViewModel.playContentLiveData.postValue(item.channelInfo)
            }
        })
        
        _binding?.channelList?.apply {
//            setPadding(leftPadding.toInt(), 0, leftPadding.toInt(), 0)
            itemAnimator = null
            adapter = mAdapter
        }
        
        observeList()
    }
    
    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.loadRecentTvChannels(isStingray,isFmRadio).map {
                it?.filter { it.channelInfo?.isExpired == false }
            }.collectLatest {
                val newList = if (!it.isNullOrEmpty()) {
                    if (showSelected) it.subList(1, it.size) else it.subList(0, it.size - 1)
                } else it
                _binding?.channelTv?.isVisible = !newList.isNullOrEmpty()
                newList?.let { mAdapter.setItems(it) }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}