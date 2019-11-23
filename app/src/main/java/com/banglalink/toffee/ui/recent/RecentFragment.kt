package com.banglalink.toffee.ui.recent

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
import com.banglalink.toffee.model.ChannelInfo
import com.facebook.shimmer.ShimmerFrameLayout

class RecentFragment:HomeBaseFragment() {
    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
        adapter.remove(channelInfo)
    }

    lateinit var adapter: CommonChannelAdapter
    lateinit var list: RecyclerView

    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var loadMoreProgress: ProgressBar
    lateinit var shimmerViewContainer: ShimmerFrameLayout
    private val viewModel by lazy{
        ViewModelProviders.of(this).get(RecentViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_recent_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shimmerViewContainer = view.findViewById(R.id.shimmer_view_container)
//        val progressDialog = VelBoxProgressDialog(context!!)
        adapter = CommonChannelAdapter(this) {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }
        list = view.findViewById(R.id.list)
        linearLayoutManager = LinearLayoutManager(activity)
        list.layoutManager = linearLayoutManager
        list.adapter = adapter
        loadMoreProgress = view.findViewById(R.id.progress_bar)
        list.addOnScrollListener(object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
           override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
               loadMoreProgress.visibility = View.VISIBLE
               loadRecentItems()
            }
        })
        activity!!.title = "Recent"
//        progressDialog.show()
        shimmerViewContainer.startShimmer();
        loadRecentItems()
        viewModel.recentLiveData.observe(viewLifecycleOwner, Observer {
//            if(progressDialog.isShowing){
//                progressDialog.dismiss()
//            }
            shimmerViewContainer.stopShimmer()
            shimmerViewContainer.visibility = View.GONE
            loadMoreProgress.visibility = View.GONE
            when(it){
                is Resource.Success->{
                    adapter.addAll(it.data)
                }
                is Resource.Failure->{
                    context!!.showToast(it.error.msg)
                }
            }
        })
    }

    private fun loadRecentItems(){
        viewModel.loadRecentItems()
    }
}