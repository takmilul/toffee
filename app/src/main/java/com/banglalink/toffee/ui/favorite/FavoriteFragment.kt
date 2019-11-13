package com.banglalink.toffee.ui.favorite

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
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.CommonChannelAdapter
import com.banglalink.toffee.ui.home.OptionCallBack
import com.banglalink.toffee.model.ChannelInfo

class FavoriteFragment:HomeBaseFragment(),OptionCallBack {
    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
        adapter?.remove(channelInfo)
    }

    lateinit var adapter: CommonChannelAdapter

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(FavoriteViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadFavoriteContent(0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_favorite_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loadMoreProgress = view.findViewById<ProgressBar>(R.id.progress_bar)
        loadMoreProgress.visibility = View.VISIBLE
        adapter = CommonChannelAdapter(this) {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }
        val list:RecyclerView = view.findViewById(R.id.list)
        val linearLayoutManager = LinearLayoutManager(activity)
        list.layoutManager = linearLayoutManager
        list.adapter = adapter
        list.addOnScrollListener(object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                loadMoreProgress.visibility = View.VISIBLE
                loadFavoriteContent(adapter.getOffset())
            }
        })
        activity!!.title = "Favorites"

        viewModel.favoriteListLiveData.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Success->{
                    loadMoreProgress.visibility = View.GONE
                    adapter?.addAll(it.data)
                }
                is Resource.Failure->{
                    activity?.showToast(it.error.msg)
                }
            }
        })

    }

    //we are overriding super's function because we need to remove from adapter as well.....need to work on it
    override fun observeFavoriteLiveData(){
        baseViewModel.favoriteLiveData.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Success->{
                    val channelInfo = it.data
                    adapter.remove(channelInfo)
                    when(channelInfo.favorite){
                        "0"->context?.showToast("Content successfully removed from favorite list")
                        "1"->context?.showToast("Content successfully added to favorite list")
                    }
                }
                is Resource.Failure->{
                    context?.showToast(it.error.msg)
                }
            }
        })
    }


    private fun loadFavoriteContent(offset:Int){
        viewModel.loadFavoriteContents(offset)
    }
}