package com.banglalink.toffee.ui.channels

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentChannelListBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.StickyHeaderGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEmpty

@AndroidEntryPoint
class ChannelFragment:BaseFragment(), ChannelStickyListAdapter.OnItemClickListener {

    private var title: String? = null
    private var subCategoryID: Int = 0
    private var category: String? = null
    private var subCategory: String? = null
    private lateinit var binding: FragmentChannelListBinding
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val channelViewModel by activityViewModels<AllChannelsViewModel>()
    
    companion object{
        fun createInstance(subCategoryID: Int, subCategory: String, category: String, showSelected: Boolean = false): ChannelFragment {
            val channelListFragment = ChannelFragment()
            val bundle = Bundle()
            bundle.putInt("sub-category-id", subCategoryID)
            bundle.putString("sub-category", subCategory)
            bundle.putString("category", category)
            bundle.putString("title", "TV Channels")
            bundle.putBoolean("show_selected", showSelected)
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
        this.subCategory = requireArguments().getString("sub-category")
        this.subCategoryID = requireArguments().getInt("sub-category-id")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChannelListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title?.let {
            activity?.title = it
        }
        binding.progressBar.show()
        
        setupEmptyView()
        
        val channelAdapter = ChannelStickyListAdapter(requireContext(), this)
        
        with(binding.gridView){
            setHasFixedSize(true)
            val gridLayoutManager = StickyHeaderGridLayoutManager(3)
            gridLayoutManager.setHeaderBottomOverlapMargin(resources.getDimensionPixelSize(R.dimen.header_shadow_size))
            layoutManager = gridLayoutManager
            adapter = channelAdapter
        }

//        homeViewModel.getChannelByCategory(0)
        //we will observe channel live data from home activity

        Log.e("CHANNEL", channelViewModel.toString())

        lifecycleScope.launchWhenStarted {
            with(channelViewModel(0)){
                collectLatest { tvList ->
                    val res = tvList.groupBy { it.categoryName }.map {
                        val categoryName = it.key
                        val categoryList = it.value.map { ci -> ci.channelInfo }
                        StickyHeaderInfo(categoryName, categoryList)
                    }
                    binding.progressBar.hide()
                    channelAdapter.setItems(res)
                }
                catch {
                    binding.progressBar.hide()
                    binding.emptyView.show()
                    it.message?.let { errorMessage -> requireContext().showToast(errorMessage) }
                }
                onEmpty {
                    binding.progressBar.hide()
                    binding.emptyView.show()
                }
            }
        }

        if(arguments?.getBoolean("show_selected") == true) {
            observe(channelViewModel.selectedChannel) {
                channelAdapter.setSelected(it)
            }
        }
    }
    
    override fun onItemClicked(channelInfo: ChannelInfo) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(channelInfo)
    }
    
    private fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(0, "No item found")
    }
    
    private fun setupEmptyView() {
        val info = getEmptyViewInfo()
        if (info.first > 0) {
            binding.emptyViewIcon.setImageResource(info.first)
        }
        else {
            binding.emptyViewIcon.visibility = View.GONE
        }

        info.second?.let {
            binding.emptyViewLabel.text = it
        }
    }
}