package com.banglalink.toffee.common.paging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentBaseSingleListBinding
import com.banglalink.toffee.ui.common.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseListFragment<T: Any>: BaseFragment() {
    protected abstract val mAdapter: BasePagingDataAdapter<T>
    protected abstract val mViewModel: BasePagingViewModel<T>

    private lateinit var binding: FragmentBaseSingleListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_base_single_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString(ARG_TITLE)?.let {
            activity?.title = it
        }

        setupEmptyView()

        setupListView()

        observeList()
    }

    protected open fun getRecyclerLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context)
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

            mAdapter.addLoadStateListener {
                binding.progressBar.isVisible = it.source.refresh is LoadState.Loading
            }

            setHasFixedSize(true)
//            setEmptyView(binding.emptyView)

//          TODO: Inspect for gridview
//           setItemViewCacheSize(10)

            adapter = mAdapter
        }
    }

    private fun observeList() {
        lifecycleScope.launch {
            mViewModel.getListData().collectLatest {
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