package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.util.Log
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
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.unsafeLazy
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.DefaultSliderView

class LandingPageFragment : HomeBaseFragment(),BaseSliderView.OnSliderClickListener {
    override fun onSliderClick(slider: BaseSliderView?) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue((slider as DefaultSliderView).data as ChannelInfo)
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
        popularVideoListAdapter.remove(channelInfo)
    }

    private lateinit var channelAdapter: ChannelAdapter
    lateinit var popularVideoListAdapter: PopularVideoListAdapter
    private var imageSlider: SliderLayout?=null
    private var popularVideoListView: RecyclerView? = null
    private var channelListView: RecyclerView? = null
    private var bottomProgress: ProgressBar? = null

    private lateinit var channelScrollListener : EndlessRecyclerViewScrollListener

    private lateinit var popularVideoScrollListener : EndlessRecyclerViewScrollListener


    val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(LandingPageViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channelAdapter = ChannelAdapter {
            //handle channel click in adapter. Basically notify livedata to homeactivity to playChannel channel
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }
        popularVideoListAdapter = PopularVideoListAdapter(this) {
            //handle video click in adapter. Basically notify livedata to homeactivity to playChannel channel
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }
        popularVideoListAdapter.setHasStableIds(true)
        viewModel.loadPopularVideos()
        viewModel.loadChannels()
        viewModel.loadFeatureContents()
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
        imageSlider = view.findViewById(R.id.slider)


        val listLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        channelListView = view.findViewById<RecyclerView>(R.id.channel_list).apply {
            layoutManager = listLayoutManager
            adapter = channelAdapter
        }

        channelScrollListener =  object:EndlessRecyclerViewScrollListener(listLayoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.loadChannels()
            }
        }
        channelListView?.addOnScrollListener(channelScrollListener)

        val linearLayoutManager = LinearLayoutManager(context)
        popularVideoListView = view.findViewById<RecyclerView>(R.id.listview).apply {
            layoutManager = linearLayoutManager
            adapter = popularVideoListAdapter
        }
        popularVideoListView?.setItemViewCacheSize(10)//We are defining offscreen cache size
        popularVideoListView?.setHasFixedSize(true)

        popularVideoScrollListener =  object:EndlessRecyclerViewScrollListener(linearLayoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                bottomProgress?.visibility = View.VISIBLE
                viewModel.loadPopularVideos()
            }
        }
        popularVideoListView?.addOnScrollListener(popularVideoScrollListener)


        bottomProgress = view.findViewById(R.id.progress_bar)
        if (popularVideoListAdapter.itemCount == 0)
            bottomProgress?.visibility = View.VISIBLE

        observeFeatureContentList()
        observeChannelList()
        observePopularVideoList()

        view.findViewById<View>(R.id.channel_view_all).setOnClickListener {
            homeViewModel.viewAllChannelLiveData.postValue(true)
        }

    }

    private fun observeChannelList() {
        viewModel.channelLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    channelAdapter.addAll(it.data)
                }
                is Resource.Failure -> {
                    channelScrollListener.resetState()
                    Log.e("LOG", it.error.msg)
                }
            }
        })
    }

    private fun observeFeatureContentList() {
        viewModel.featureContentLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                  for(channelInfo in it.data){
                      val textSliderView = DefaultSliderView(activity, channelInfo)
                      textSliderView
                          .description(channelInfo.program_name)
                          .image(channelInfo.feature_image).scaleType = BaseSliderView.ScaleType.Fit

                      //add your extra information
                      textSliderView.bundle(Bundle())
                      textSliderView.bundle
                          .putString("extra", channelInfo.program_name)

                      textSliderView.setOnSliderClickListener(this)
                      imageSlider?.addSlider<DefaultSliderView>(textSliderView)
                  }
                }
                is Resource.Failure -> {
                    Log.e("LOG", it.error.msg)
                }
            }
        })
    }

    private fun observePopularVideoList() {
        viewModel.popularVideoLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    bottomProgress?.visibility = View.GONE
                    if (popularVideoListAdapter.itemCount == 0) {
                        val fakeChannelInfo =
                            ChannelInfo()//we are adding fake channelinfo because of header in adapter....
                        fakeChannelInfo.id = System.currentTimeMillis().toString()
                        popularVideoListAdapter.add(fakeChannelInfo)
                    }
                    if (it.data.isNotEmpty()) {
                        popularVideoListAdapter.addAll(it.data)
                    }

                }
                is Resource.Failure -> {
                    popularVideoScrollListener.resetState()
                   context?.showToast(it.error.msg)
                }
            }
        })
    }

    fun onBackPressed(): Boolean {
        if(popularVideoListView!=null && popularVideoListView!!.computeVerticalScrollOffset() > 0){
            popularVideoListView?.smoothScrollToPosition(0)
            return true
        }
        return false
    }

    override fun onDestroyView() {
        popularVideoListView?.adapter = null
        channelListView?.adapter = null
        popularVideoListView?.clearOnScrollListeners()
        channelListView?.clearOnScrollListeners()
        popularVideoListView = null
        channelListView = null
        imageSlider?.stopAutoCycle()
        imageSlider?.removeAllSliders()
        imageSlider = null
        bottomProgress = null
        super.onDestroyView()

    }
}