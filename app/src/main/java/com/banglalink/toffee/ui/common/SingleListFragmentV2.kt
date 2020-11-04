package com.banglalink.toffee.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentCommonSingleListV2Binding
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.android.synthetic.main.fragment_common_single_list_v2.*
import kotlinx.android.synthetic.main.fragment_top_panel_search_collapsed.*

abstract class SingleListFragmentV2<T: Any> : Fragment() {

    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    lateinit var binding: FragmentCommonSingleListV2Binding

    protected lateinit var mAdapter: MyBaseAdapterV2<T>
    protected lateinit var mViewModel: SingleListViewModel<T>
    private val mHomeViewModel by activityViewModels<HomeViewModel>()

    companion object {
        const val ARG_TITLE = "arg-title"
    }

    abstract fun initAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_common_single_list_v2, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()

        arguments?.getString(ARG_TITLE)?.let {
            activity?.title = it
        }

        setupEmptyView()

        setupListView()

        observeList()

        mViewModel.loadData()
    }

    private fun setupListView() {
        with(binding.listview) {
            adapter = mAdapter

            val listLayoutManager = getRecyclerLayoutManager()
            layoutManager = listLayoutManager

            if(shouldLoadMore) {
                if (listLayoutManager is LinearLayoutManager) {
                    scrollListener = object : EndlessRecyclerViewScrollListener(listLayoutManager) {
                        override fun onLoadMore(
                            page: Int,
                            totalItemsCount: Int,
                            view: RecyclerView
                        ) {
                            mViewModel.loadData()
                        }
                    }
                } else if (listLayoutManager is GridLayoutManager) {
                    scrollListener = object : EndlessRecyclerViewScrollListener(listLayoutManager) {
                        override fun onLoadMore(
                            page: Int,
                            totalItemsCount: Int,
                            view: RecyclerView
                        ) {
                            mViewModel.loadData()
                        }
                    }
                }
                addOnScrollListener(scrollListener)
            }
            setHasFixedSize(true)
//          TODO: Inspect for gridview
//           setItemViewCacheSize(10)
        }
    }

    protected open val shouldLoadMore = true

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

    fun playVideoChannel(T: Any) {
        if(T is ChannelInfo) {
            mHomeViewModel.fragmentDetailsMutableLiveData.postValue(T)
        }
    }

    private fun observeList() {
        mViewModel.listData.observe(viewLifecycleOwner, Observer {
            when(it) {
                is Resource.Success -> {
                    val itemCount = mAdapter.itemCount
                    if(it.data.isEmpty() && itemCount == 0) {
                        binding.emptyView.visibility = View.VISIBLE
                    } else {
                        binding.emptyView.visibility = View.GONE
                    }
                    mAdapter.addAll(it.data)
                }
                is Resource.Failure -> {
                    scrollListener.resetState()
                    activity?.showToast(it.error.msg)
                }
            }
        })

        mViewModel.showProgress.observe(viewLifecycleOwner, Observer {
            if(it) {
                showProgress()
            } else{
                hideProgress()
            }
        })
    }

    private fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        binding.listview.adapter = null
        binding.listview.clearOnScrollListeners()
        binding.unbind()
        super.onDestroyView()
    }
}