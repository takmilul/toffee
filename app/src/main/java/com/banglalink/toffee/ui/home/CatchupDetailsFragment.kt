package com.banglalink.toffee.ui.home

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
import com.banglalink.toffee.model.ChannelInfo

class CatchupDetailsFragment:HomeBaseFragment() {
    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
        mAdapter.remove(channelInfo)
    }

    companion object{
        const val CHANNEL_INFO = "channel_info_"
        fun createInstance(channelInfo: ChannelInfo): CatchupDetailsFragment {
            val catchupFragment = CatchupDetailsFragment()
            val bundle = Bundle()
            bundle.putParcelable(CHANNEL_INFO, channelInfo)
            catchupFragment.arguments = bundle
            return catchupFragment
        }
    }
    lateinit var mAdapter: CommonChannelAdapter
    private var currentItem: ChannelInfo? = null
    lateinit var progressBar: ProgressBar

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(CatchupDetailsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentItem = arguments?.getParcelable(CHANNEL_INFO)
        getContents(0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_catchupdetails, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = CommonChannelAdapter(this) {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }
        val listView:RecyclerView = view.findViewById(R.id.listview)
        progressBar = view.findViewById(R.id.progress_bar)
        val linearLayoutManager = LinearLayoutManager(activity)
        listView.layoutManager = linearLayoutManager
        listView.adapter = mAdapter

        // Adds the scroll listener to RecyclerView
        listView.addOnScrollListener(object :
            EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                progressBar.visibility = View.VISIBLE
                getContents(mAdapter.getOffset())
            }
        })
        viewModel.relativeContentLiveData.observe(viewLifecycleOwner, Observer {
            progressBar.visibility = View.GONE
            when(it){
                is Resource.Success->{
                    mAdapter?.addAll(it.data)
                }
                is Resource.Failure->{
                    context!!.showToast(it.error.msg)
                }
            }
        })
        progressBar.visibility = View.VISIBLE
    }

    private fun getContents(offset:Int){
        viewModel.getContents(currentItem!!,offset)

    }
}