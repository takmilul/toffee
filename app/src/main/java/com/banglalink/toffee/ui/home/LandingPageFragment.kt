package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.util.unsafeLazy
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.DefaultSliderView

class LandingPageFragment : HomeBaseFragment(),BaseSliderView.OnSliderClickListener {

    companion object {
        fun newInstance(): LandingPageFragment {
            return LandingPageFragment()
        }
    }

    override fun onSliderClick(slider: BaseSliderView?) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue((slider as DefaultSliderView).data as ChannelInfo)
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
//        popularVideoListAdapter.remove(channelInfo)
    }

//    lateinit var popularVideoListAdapter: PopularVideoListAdapter
//    private var popularVideoListView: RecyclerView? = null
//    private var bottomProgress: ProgressBar? = null
//    private lateinit var popularVideoScrollListener : EndlessRecyclerViewScrollListener


    lateinit var viewModel: LandingPageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(activity!!).get(LandingPageViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_page2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Home"

//        viewModel.loadChannels()
//        viewModel.loadPopularVideos()
//        viewModel.loadCategories()
//        viewModel.loadFeatureContents()
//        viewModel.loadUserChannels()
        viewModel.loadMostPopularVideos()
    }

    fun onBackPressed(): Boolean {

        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}