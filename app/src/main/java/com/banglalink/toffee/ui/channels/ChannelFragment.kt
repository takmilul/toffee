package com.banglalink.toffee.ui.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.databinding.FragmentChannelListBinding
import com.banglalink.toffee.enums.PlayingPage.FM_RADIO
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.ifNotNullOrEmpty
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.StickyHeaderGridLayoutManager
import com.banglalink.toffee.util.BindingUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChannelFragment:BaseFragment(), ChannelStickyListAdapter.OnItemClickListener {
    
    private var title: String? = null
    private var subCategoryID: Int = 0
    private var category: String? = null
    private val binding get() = _binding!!
    private var isFmRadio: Boolean = false
    private var subCategory: String? = null
    private var isStingray: Boolean = false
    @Inject lateinit var bindingUtil: BindingUtil
    private var _binding: FragmentChannelListBinding ? = null
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val channelViewModel by activityViewModels<AllChannelsViewModel>()
    
    companion object{
        fun createInstance(subCategoryID: Int, subCategory: String, category: String, showSelected: Boolean = false, isStingray: Boolean = false,isFmRadio: Boolean = false): ChannelFragment {
            val channelListFragment = ChannelFragment()
            val bundle = Bundle().apply {
                putInt("sub_category_id", subCategoryID)
                putString("sub_category", subCategory)
                putString("category", category)
                putString("title", if(isStingray) "Music Videos" else if (isFmRadio) "FM Radio" else "TV Channels")
                putBoolean("show_selected", showSelected)
                putBoolean("is_stingray", isStingray)
                putBoolean("is_fmRadio", isFmRadio)
            }
            channelListFragment.arguments = bundle
            return channelListFragment
        }
        
        fun createInstance(category: String, showSelected: Boolean = false): ChannelFragment {
            val bundle = Bundle()
            val instance = ChannelFragment()
            bundle.putString("category", category)
            bundle.putBoolean("show_selected", showSelected)
            instance.arguments = bundle
            return instance
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = requireArguments().getString("title")
        this.category = requireArguments().getString("category")
        this.subCategory = requireArguments().getString("sub_category")
        this.subCategoryID = requireArguments().getInt("sub_category_id")
        this.isStingray = requireArguments().getBoolean("is_stingray")
        this.isFmRadio = requireArguments().getBoolean("is_fmRadio")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChannelListBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.progressBar?.load(R.drawable.content_loader)
        title?.let {
            activity?.title = it
        }
        _binding?.listview?.hide()
        _binding?.progressBar?.show()
        homeViewModel.isStingray.postValue(isStingray)
        homeViewModel.isFmRadio.postValue(isFmRadio)
        setupEmptyView()
        
        val channelAdapter = ChannelStickyListAdapter(requireContext(), this, bindingUtil)
        
        _binding?.listview?.apply{
            // set top padding for fm radio because the sticky header and recently viewed items will be hidden
            if (isFmRadio && homeViewModel.currentlyPlayingFrom.value == FM_RADIO) {
                setPadding(0, 20.px, 0, 0)
            } else {
                setPadding(0, 0, 0, 0)
            }
            setHasFixedSize(true)
            itemAnimator = null
            val gridLayoutManager = StickyHeaderGridLayoutManager(3)
            gridLayoutManager.setHeaderBottomOverlapMargin(resources.getDimensionPixelSize(R.dimen.header_shadow_size))
            layoutManager = gridLayoutManager
            adapter = channelAdapter
        }
        
//        homeViewModel.getChannelByCategory(0)
        //we will observe channel live data from home activity
        
        viewLifecycleOwner.lifecycleScope.launch {
            with(channelViewModel.getChannels(0)) {
                collectLatest { tvList ->
                    val res = tvList?.groupBy { it.categoryName.trimIndent() }?.map {
                        val categoryName = it.key.trimIndent()
                        val categoryList = it.value.map { ci -> ci.channelInfo }
                        StickyHeaderInfo(categoryName, categoryList)
                    }
                    res?.ifNotNullOrEmpty {
                        _binding?.progressBar?.hide()
                        _binding?.listview?.show()
                        channelAdapter.setItems(it.toList())
                    }
                }
                catch {
                    _binding?.progressBar?.hide()
                    _binding?.emptyView?.show()
                    it.message?.let { errorMessage -> requireContext().showToast(errorMessage) }
                }
                onEmpty {
                    _binding?.progressBar?.hide()
                    _binding?.emptyView?.show()
                }
            }
        }
        
        if(arguments?.getBoolean("show_selected") == true) {
            observe(channelViewModel.selectedChannel) {
                channelAdapter.setSelected(it)
            }
        }
        ToffeeAnalytics.logEvent(
            ToffeeEvents.SCREEN_VIEW,
            bundleOf(FirebaseParams.BROWSER_SCREEN to "channel_list"))
    }
    
    override fun onItemClicked(channelInfo: ChannelInfo) {
        homeViewModel.playContentLiveData.postValue(channelInfo)
    }
    
    private fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(0, "No item found")
    }
    
    private fun setupEmptyView() {
        val info = getEmptyViewInfo()
        if (info.first > 0) {
            _binding?.emptyViewIcon?.setImageResource(info.first)
        }
        else {
            _binding?.emptyViewIcon?.visibility = View.GONE
        }

        info.second?.let {
            _binding?.emptyViewLabel?.text = it
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}