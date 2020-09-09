package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.CategoriesListAdapter
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.android.synthetic.main.fragment_landing_categories.*

class LandingCategoriesFragment: HomeBaseFragment() {
    private lateinit var mAdapter: CategoriesListAdapter

    val viewModel by unsafeLazy {
        ViewModelProvider(activity!!)[LandingPageViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = CategoriesListAdapter(this) {
            homeViewModel.openCategoryLiveData.value = it
        }

        viewAllButton.setOnClickListener {
            homeViewModel.viewAllCategories.postValue(true)
        }

        with(categoriesList) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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