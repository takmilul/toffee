package com.banglalink.toffee.common.paging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.databinding.FragmentBaseSingleListBinding
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import kotlinx.coroutines.flow.collectLatest

abstract class BaseListFragment<T: Any>: BaseFragment() {
    protected abstract val mAdapter: BasePagingDataAdapter<T>
    protected abstract val mViewModel: BasePagingViewModel<T>

    protected lateinit var binding: FragmentBaseSingleListBinding

    open val itemMargin = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBaseSingleListBinding.inflate(inflater, container, false)
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
            binding.emptyViewIcon.visibility = View.GONE
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

            mAdapter.addLoadStateListener {
                if(it.source.refresh is LoadState.Loading) {
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    binding.progressBar.visibility = View.GONE
                }

                mAdapter.apply {
                    val showEmpty = itemCount <= 0 && !it.source.refresh.endOfPaginationReached
                    binding.emptyView.isGone = !showEmpty
                    binding.listview.isVisible = !showEmpty
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
        binding.listview.adapter = null
        binding.listview.clearOnScrollListeners()
        binding.unbind()
        super.onDestroyView()
    }

    companion object {
        const val ARG_TITLE = "arg-title"
    }
}