package com.banglalink.toffee.common.paging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.filter
import androidx.paging.map
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.databinding.FragmentBaseSingleListBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseListFragment<T : Any> : BaseFragment() {
    
    private val gson = Gson()
    protected abstract val mAdapter: BasePagingDataAdapter<T>
    protected abstract val mViewModel: BasePagingViewModel<T>
    private var _binding: FragmentBaseSingleListBinding? = null
    protected val binding get() = _binding!!
    @Inject lateinit var localSync: LocalSync
    open val itemMargin = 0
    open val verticalPadding = Pair(0, 0)
    open val horizontalPadding = Pair(0, 0)
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentBaseSingleListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        arguments?.getString(ARG_TITLE)?.let {
            activity?.title = it
        }
        
        setEmptyView()
        setupListView()
        observeList()
    }
    
    protected open fun getRecyclerLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context)
    }
    
    protected open fun setEmptyView() {
        setupEmptyView()
    }
    
    protected open fun getEmptyViewInfo(): Triple<Int, String?, String?> {
        return Triple(0, null, "No item found")
    }
    
    private fun setupEmptyView() {
        val info = getEmptyViewInfo()
        if (info.first > 0) {
            binding.emptyViewIcon.setImageResource(info.first)
        } else {
            binding.emptyViewIcon.hide()
        }
        
        info.second?.let {
            binding.emptyViewLabelLarge.text = it
            binding.emptyViewLabelLarge.isVisible = true
        }
        info.third?.let {
            binding.emptyViewLabel.text = it
        }
    }

    fun setResultView(text: String?) {
        text?.let {
            binding.topPanel.searchResult.text = it
            binding.topPanel.topPanelContainer.show()
        }
    }
    
    private fun setupListView() {
        with(binding.listview) {
            
            val listLayoutManager = getRecyclerLayoutManager()
            layoutManager = listLayoutManager
            
            if (itemMargin > 0) {
                addItemDecoration(MarginItemDecoration(itemMargin))
            }
            
            updatePadding(top = verticalPadding.first.px, bottom = verticalPadding.second.px)
            updatePadding(left = horizontalPadding.first.px, right = horizontalPadding.second.px)
            binding.progressBar.load(R.drawable.content_loader)
            
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow
//                    .distinctUntilChangedBy { it.refresh }
                    .collectLatest {
                        binding.progressBar.isVisible = it.source.refresh is LoadState.Loading
                        mAdapter.apply {
                            val showEmpty = itemCount <= 0 && !it.source.refresh.endOfPaginationReached && it.source.refresh !is LoadState.Loading
                            binding.emptyView.isVisible = showEmpty
                            binding.listview.isVisible = !showEmpty
                        }
                    }
            }
            
            setHasFixedSize(true)
//            setEmptyView(binding.emptyView)
            
//          TODO: Inspect for gridview
//           setItemViewCacheSize(10)
            
            adapter = getRecyclerAdapter()
        }
    }
    
    open fun getRecyclerAdapter(): RecyclerView.Adapter<*> {
        return mAdapter.withLoadStateFooter(ListLoadStateAdapter { mAdapter.retry() })
    }
    
    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.getListData().collectLatest {
                mAdapter.submitData(it.filter {
                    (!(it is ChannelInfo && it.isExpired) && !(it is UserActivities && gson.fromJson(it.payload, ChannelInfo::class.java).isExpired))
                }.map {
                    var isHeaderTextSet = false
                    if (it is ChannelInfo) {
                        localSync.syncData(it as ChannelInfo)
                        if(it.totalCount > 1 && !isHeaderTextSet) {
                            setResultView("${it.totalCount} results found")
                            isHeaderTextSet = true
                        }
                        else if(it.totalCount == 1 && !isHeaderTextSet) {
                            setResultView("${it.totalCount} result found")
                            isHeaderTextSet = true
                        }
                    }
                    it
                })
            }
        }
    }
    
    override fun onDestroyView() {
        binding.listview.adapter = null
        binding.listview.clearOnScrollListeners()
        binding.unbind()
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        const val ARG_TITLE = "arg-title"
    }
}