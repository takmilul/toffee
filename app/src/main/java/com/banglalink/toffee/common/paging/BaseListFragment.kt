package com.banglalink.toffee.common.paging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.databinding.FragmentBaseSingleListBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import kotlinx.coroutines.flow.collectLatest

abstract class BaseListFragment<T: Any>: BaseFragment() {
    protected abstract val mAdapter: BasePagingDataAdapter<T>
    protected abstract val mViewModel: BasePagingViewModel<T>

    private var _binding: FragmentBaseSingleListBinding? = null
    protected val binding get() = _binding!!

    open val itemMargin = 0
    open val verticalPadding = Pair(0,0)
    open val horizontalPadding = Pair(0,0)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

    protected open fun setEmptyView(){
        setupEmptyView()
    }
    
    protected open fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(0, "No item found")
    }

    private fun setupEmptyView() {
        val info = getEmptyViewInfo()
        if(info.first > 0) {
            binding.emptyViewIcon.setImageResource(info.first)
        }
        else {
            binding.emptyViewIcon.hide()
        }

        info.second?.let {
            binding.emptyViewLabel.text = it
        }
    }

    private fun setupListView() {
        with(binding.listview) {

            val listLayoutManager = getRecyclerLayoutManager()
            layoutManager = listLayoutManager

            if(itemMargin > 0) {
                addItemDecoration(MarginItemDecoration(itemMargin))
            }
            
            updatePadding(top = verticalPadding.first.px, bottom = verticalPadding.second.px)
            updatePadding(left = horizontalPadding.first.px, right = horizontalPadding.second.px)
            
            mAdapter.addLoadStateListener(loadStateListener)

            setHasFixedSize(true)
//            setEmptyView(binding.emptyView)

//          TODO: Inspect for gridview
//           setItemViewCacheSize(10)

            adapter = getRecyclerAdapter()
        }
    }

    private val loadStateListener : (CombinedLoadStates) -> Unit = {
        binding.progressBar.isVisible = it.source.refresh is LoadState.Loading
        mAdapter.apply {
            val showEmpty = itemCount <= 0 && !it.source.refresh.endOfPaginationReached && it.source.refresh !is LoadState.Loading
            binding.emptyView.isVisible = showEmpty
            binding.listview.isVisible = !showEmpty
        }
    }

    open fun getRecyclerAdapter(): RecyclerView.Adapter<*> {
        return mAdapter.withLoadStateFooter( ListLoadStateAdapter{ mAdapter.retry() } )
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            mViewModel.getListData.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    override fun onDestroyView() {
        mAdapter.removeLoadStateListener(loadStateListener)
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