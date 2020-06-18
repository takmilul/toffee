package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.CommonSingleListFragment
import com.banglalink.toffee.util.unsafeLazy

class CatchupDetailsFragment:CommonSingleListFragment() {

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
    private var currentItem: ChannelInfo? = null

    private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(CatchupDetailsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentItem = arguments?.getParcelable(CHANNEL_INFO)
    }

    override fun initAdapter() {
        mAdapter = CatchUpDetailsAdapter(this) {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }
    }

    override fun updateHeader() {
        if(currentItem!=null)
            mAdapter.add(currentItem!!)//Fake item for enabling header...because we are adding header at 0
    }
    override fun hideNotInterestedMenuItem(channelInfo: ChannelInfo): Boolean {
        return currentItem?.id == channelInfo.id
    }

    override fun loadItems(): LiveData<Resource<List<ChannelInfo>>> {
        return viewModel.getContents(currentItem!!)
    }

}