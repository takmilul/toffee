package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.home.OptionCallBack
import com.banglalink.toffee.ui.home.UserChannelsListAdapter
import com.banglalink.toffee.ui.useractivities.UserActivitiesMainFragment
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing_user_channels.*

@AndroidEntryPoint
class LandingUserChannelsFragment: HomeBaseFragment() {
    private lateinit var mAdapter: UserChannelsListAdapter

    val viewModel by unsafeLazy {
        ViewModelProvider(activity!!)[LandingPageViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_user_channels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = UserChannelsListAdapter(this) {
//            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
            parentFragment?.findNavController()?.navigate(R.id.action_menu_feed_to_channelRatingFragment)
        }

        viewAllButton.setOnClickListener {
            parentFragment?.findNavController()?.
            navigate(R.id.menu_activities,
            Bundle().apply {
                putInt(UserActivitiesMainFragment.ARG_SELECTED_TAB, 1)
            })
        }

        with(userChannelList) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = mAdapter
            addOnScrollListener(object : EndlessRecyclerViewScrollListener(layoutManager as LinearLayoutManager){
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                    viewModel.loadUserChannels()
                }
            })
        }

        viewModel.loadUserChannels()

        observeList()
    }

    private fun observeList() {
        viewModel.userChannelList.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    mAdapter.addAll(it.data)
                }
                is Resource.Failure -> {
                    Log.e("LOG", it.error.msg)
                }
            }
        })
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}