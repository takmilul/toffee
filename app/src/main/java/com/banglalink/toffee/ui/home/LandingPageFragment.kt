package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.player.ChannelInfo
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.DefaultSliderView

class LandingPageFragment :HomeBaseFragment(){
    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
        popularVideoListAdapter.remove(channelInfo)
    }

    lateinit var channelAdapter: ChannelAdapter
    lateinit var popularVideoListAdapter: PopularVideoListAdapter
    var imageSliderList: MutableList<DefaultSliderView> = mutableListOf()
    lateinit var imageSlider: SliderLayout
    lateinit var catchupListView: RecyclerView
    lateinit var bottomProgress: ProgressBar

    val viewModel by lazy {
        ViewModelProviders.of(this).get(LandingPageViewModel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channelAdapter = ChannelAdapter{
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }
        popularVideoListAdapter = PopularVideoListAdapter(this){
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }
        viewModel.loadPopularVideos(popularVideoListAdapter.itemCount)
        viewModel.loadChannels(channelAdapter.itemCount)
        viewModel.loadFeatureContents(0)
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
        val listLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        val channelListView = view.findViewById<RecyclerView>(R.id.channel_list).apply {
            setHasFixedSize(true)
            layoutManager = listLayoutManager
            imageSlider = view.findViewById(R.id.slider)
            adapter = channelAdapter
        }

        channelListView.addOnScrollListener(object :
            EndlessRecyclerViewScrollListener(listLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                viewModel.loadChannels(channelAdapter.itemCount)
            }
        })

        val linearLayoutManager = LinearLayoutManager(activity)
        catchupListView = view.findViewById<RecyclerView>(R.id.listview).apply {
            layoutManager = linearLayoutManager
            adapter = popularVideoListAdapter
        }

        catchupListView.addOnScrollListener(object :
            EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                bottomProgress.visibility = View.VISIBLE
                viewModel.loadPopularVideos(popularVideoListAdapter.getOffset())
            }
        })

        bottomProgress = view.findViewById(R.id.progress_bar)
        bottomProgress.visibility = View.VISIBLE

        observeFeatureContentList()
        observeChannelList()
        observePopularVideoList()

        view.findViewById<View>(R.id.channel_view_all).setOnClickListener{
            homeViewModel.viewAllChannelLiveData.postValue(true)
        }

    }

    private fun observeChannelList(){
        viewModel.channelLiveData.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Success ->{
                    channelAdapter.addAll(it.data)
                }
                is Resource.Failure->{
                    Log.e("LOG",it.error.msg)
                }
            }
        })
    }

    private fun observeFeatureContentList(){
        viewModel.featureContentLiveData.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Success ->{
                    for(channelInfo in it.data){
                        val textSliderView = DefaultSliderView(activity, channelInfo)
                        textSliderView
                            .description(channelInfo.program_name)
                            .image(channelInfo.feature_image).scaleType = BaseSliderView.ScaleType.Fit

                        //add your extra information
                        textSliderView.bundle(Bundle())
                        textSliderView.bundle
                            .putString("extra", channelInfo.program_name)

//                        imageSlider.addSlider(textSliderView);
                        imageSliderList.add(textSliderView)
                    }
                    loadSlider()
                }
                is Resource.Failure->{
                    Log.e("LOG",it.error.msg)
                }
            }
        })
    }

    private fun observePopularVideoList(){
        viewModel.popularVideoLiveData.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Success ->{
                    bottomProgress.visibility = View.GONE
                    if(popularVideoListAdapter.itemCount == 0){
                        val fakeChannelInfo = ChannelInfo()//we are adding fake channelinfo because of header in adapter....
                        popularVideoListAdapter.add(fakeChannelInfo)
                    }
                    popularVideoListAdapter.addAll(it.data)
                }
                is Resource.Failure->{
                    Log.e("LOG",it.error.msg)
                }
            }
        })
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadSlider()
    }

    private fun loadSlider() {
        for (defaultSliderView in imageSliderList) {
            imageSlider.addSlider(defaultSliderView)
        }
    }

    fun onBackPressed(): Boolean {
        if (catchupListView.computeVerticalScrollOffset() > 0) {
            catchupListView.smoothScrollToPosition(0)
            return true
        }
        return false
    }
}