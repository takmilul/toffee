package com.banglalink.toffee.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.CommonChannelAdapter
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.player.ChannelInfo

class SearchFragment:HomeBaseFragment() {

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(SearchViewModel::class.java)
    }

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

    lateinit var adapter: CommonChannelAdapter

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
        adapter?.remove(channelInfo)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_list, container, false)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = "Search"
        val list = view.findViewById<View>(R.id.list) as RecyclerView
        val linearLayoutManager = LinearLayoutManager(activity)
        list.layoutManager = linearLayoutManager
        adapter = CommonChannelAdapter(this){
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }
        list.adapter = adapter
        val  loadMoreProgress = view.findViewById(R.id.progress_bar) as ProgressBar
        loadMoreProgress.visibility = View.VISIBLE
        val keyWord = arguments?.getString(SEARCH, "")!!
        search(keyWord)
        list.addOnScrollListener(object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
           override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
               loadMoreProgress.visibility = View.VISIBLE
                search(keyWord)
            }
        })

        viewModel.searchResultLiveData.observe(viewLifecycleOwner, Observer {
            loadMoreProgress.visibility = View.GONE
            when(it){
                is Resource.Success->{
                    adapter.addAll(it.data)
                }
                is Resource.Failure->{
                    context?.showToast(it.error.msg)
                }
            }

        })

    }

    fun search(searchKey:String){
        viewModel.searchContent(searchKey,adapter.getOffset())
    }


}