package com.banglalink.toffee.ui.recent

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.CommonSingleListFragment

class RecentFragment:CommonSingleListFragment() {

    private val viewModel by lazy{
        ViewModelProviders.of(this).get(RecentViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.title = "Recent"
        loadRecentItems()
        viewModel.recentLiveData.observe(viewLifecycleOwner, Observer {
            hideProgress()
            when(it){
                is Resource.Success->{
                    mAdapter?.addAll(it.data)
                }
                is Resource.Failure->{
                    context!!.showToast(it.error.msg)
                }
            }
        })
    }

    override fun loadItems() {
        loadRecentItems()
    }

    override fun onFavoriteItemRemoved(channelInfo: ChannelInfo) {
       mAdapter?.remove(channelInfo)
    }

    private fun loadRecentItems(){
        viewModel.loadRecentItems()
    }
}