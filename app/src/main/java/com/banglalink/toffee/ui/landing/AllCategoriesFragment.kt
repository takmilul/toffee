package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.CategoriesListAdapter
import com.banglalink.toffee.ui.home.ChannelAdapter
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.android.synthetic.main.fragment_landing_categories.*
import kotlinx.android.synthetic.main.fragment_landing_tv_channels.*

class AllCategoriesFragment: HomeBaseFragment() {
    private lateinit var mAdapter: CategoriesListAdapter

    val viewModel by unsafeLazy {
        ViewModelProvider(activity!!)[LandingPageViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_categories, container, false).apply {
            setBackgroundColor(ContextCompat.getColor(context, R.color.bg_color))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = CategoriesListAdapter(this)

//        viewAllButton.setOnClickListener {
//            homeViewModel.viewAllChannelLiveData.postValue(true)
//        }

        with(categoriesList) {
            layoutManager = GridLayoutManager(context, 3)
            adapter = mAdapter

//            addOnScrollListener(object : EndlessRecyclerViewScrollListener(layoutManager as LinearLayoutManager){
//                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
//                    viewModel.loadChannels()
//                }
//            })
        }

        viewModel.loadCategories()

        observeList()
    }

    private fun observeList() {
        viewModel.categoriesLiveData.observe(viewLifecycleOwner, Observer {
            mAdapter.addAll(it)
        })
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}