package com.banglalink.toffee.ui.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.channels.adapter.ChannelStickyListAdapter
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.player.ChannelInfo
import com.banglalink.toffee.ui.widget.StickyHeaderGridLayoutManager

class ChannelFragment: HomeBaseFragment(),ChannelStickyListAdapter.OnItemClickListener {
    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
        //not needed
    }

    override fun onItemClicked(channelInfo: ChannelInfo) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(channelInfo)
    }

    private var webUrl: String? = null
    private var category: String? = null
    private var subCategory: String? = null
    private var subCategoryID: Int = 0
//    lateinit var progressDialog: VelBoxProgressDialog


    companion object{
        const val WEB_URL = "data-url"
        fun createInstance(
            subCategoryID: Int,
            subCategory: String,
            category: String
        ): ChannelFragment {
            val channelListFragment = ChannelFragment()
            val bundle = Bundle()
            bundle.putInt("sub-category-id", subCategoryID)
            bundle.putString("sub-category", subCategory)
            bundle.putString("category", category)
            channelListFragment.arguments = bundle
            return channelListFragment
        }

        fun createInstance(url: String, category: String): ChannelFragment {
            val bundle = Bundle()
            bundle.putString(WEB_URL, url)
            val instance = ChannelFragment()
            bundle.putString("category", category)
            instance.arguments = bundle
            return instance
        }
    }

    fun updateUrl(urlAllChannelList: String) {
        if (webUrl != urlAllChannelList) {
            webUrl = urlAllChannelList
           homeViewModel.getChannelByCategory(subCategoryID)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        webUrl = bundle!!.getString(WEB_URL)

        this.category = arguments!!.getString("category")
        this.subCategory = arguments!!.getString("sub-category")
        this.subCategoryID = arguments!!.getInt("sub-category-id")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_channel_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = this.category
        var channelAdapter: ChannelStickyListAdapter? = null
        val gridView: RecyclerView  = view.findViewById(R.id.gridView)
        gridView.setHasFixedSize(true)
        val layoutManager = StickyHeaderGridLayoutManager(3)
        layoutManager.setHeaderBottomOverlapMargin(resources.getDimensionPixelSize(R.dimen.header_shadow_size))
        gridView.layoutManager = layoutManager

        //we will observe channel live data from home activity
        homeViewModel.channelLiveData.observe(viewLifecycleOwner, Observer {
          when(it){
              is Resource.Success->{
                  channelAdapter = ChannelStickyListAdapter(context!!, it.data.toMutableList(), this)
                  gridView.adapter = channelAdapter
              }
              is Resource.Failure->{
                  context?.showToast(it.error.msg)
              }
          }
        })
    }
}