package com.banglalink.toffee.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentCatchupBinding
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter

abstract class CommonSingleListFragment : HomeBaseFragment() {

    lateinit var mAdapter: MyBaseAdapter<ChannelInfo>
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener

    lateinit var binding: FragmentCatchupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_catchup, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("title")?.let {
            activity?.title = it
        }
        initAdapter()
        val linearLayoutManager = LinearLayoutManager(context)
        binding.listview.layoutManager = linearLayoutManager
        binding.listview.adapter = mAdapter
        binding.listview.addItemDecoration(MarginItemDecoration(12))
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                loadChannelList()
            }
        }
        // Adds the scroll listener to RecyclerView
        binding.listview.addOnScrollListener(scrollListener)
        binding.listview.setHasFixedSize(true)
        binding.listview.setItemViewCacheSize(10)
        loadChannelList()
    }

    open fun initAdapter(){
        mAdapter = CommonChannelAdapter(this) {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }
    }

    fun loadChannelList() {
        showProgress()
        loadItems().observe(viewLifecycleOwner, Observer {
            hideProgress()
            when (it) {
                is Resource.Success -> {
                    val itemCount = mAdapter.itemCount
                    if(itemCount==0){
                        updateHeader()//Working around header fo CatchupDetailsFragment. We need to add a header item in adapter
                    }
                    if (it.data.isEmpty() && itemCount == 0) {
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
    }

    private fun showProgress() {
        binding.progress.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    abstract fun loadItems(): LiveData<Resource<List<ChannelInfo>>>

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
        binding.progress.visibility = View.GONE
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
        mAdapter.remove(channelInfo)
        if (mAdapter.itemCount == 0) {
            binding.emptyView.visibility = View.VISIBLE
        }
    }

    override fun handleFavoriteRemovedSuccessFully(channelInfo: ChannelInfo) {
        if (removeUnFavoriteItemFromList()) {
            mAdapter.remove(channelInfo)
            if (mAdapter.itemCount == 0) {
                binding.emptyView.visibility = View.VISIBLE
            }
        }
    }
    open fun updateHeader(){
        //hook for sub class
    }

    //hook for removing item when set to unfavorite. Subclass can override it to change the behavior
    open fun removeUnFavoriteItemFromList(): Boolean {
        return false
    }

    override fun viewAllVideoClick() {
        homeViewModel.viewAllVideoLiveData.postValue(true)
    }

    override fun onDestroyView() {
        binding.listview?.adapter = null
        binding.listview?.clearOnScrollListeners()
        binding.unbind()
        super.onDestroyView()
    }
}