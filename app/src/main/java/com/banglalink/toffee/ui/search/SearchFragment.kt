package com.banglalink.toffee.ui.search

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.CommonSingleListFragment

class SearchFragment:CommonSingleListFragment() {
    override fun onFavoriteItemRemoved(channelInfo: ChannelInfo) {
       //not handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        searchKey = arguments?.getString(SEARCH, "")!!
        super.onCreate(savedInstanceState)
    }
    override fun loadItems(offset: Int) {
        viewModel.searchContent(searchKey,offset)
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(SearchViewModel::class.java)
    }

    lateinit var searchKey: String

    companion object{
        const val SEARCH = "_search_"
        fun createInstance(search: String): SearchFragment {
            val searchListFragment = SearchFragment()
            val bundle = Bundle()
            bundle.putString(SEARCH, search)
            searchListFragment.arguments = bundle
            return searchListFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = "Search"
        viewModel.searchResultLiveData.observe(viewLifecycleOwner, Observer {
            hideProgress()
            when(it){
                is Resource.Success->{
                    mAdapter?.addAll(it.data)
                }
                is Resource.Failure->{
                    context?.showToast(it.error.msg)
                }
            }

        })

    }
}