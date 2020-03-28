package com.banglalink.toffee.ui.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.StickyHeaderGridLayoutManager
import com.banglalink.toffee.util.unsafeLazy

class ChannelFragment:Fragment(), ChannelStickyListAdapter.OnItemClickListener {

    private val homeViewModel by unsafeLazy {
        ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
    }

    override fun onItemClicked(channelInfo: ChannelInfo) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(channelInfo)
    }

    private var category: String? = null
    private var subCategory: String? = null
    private var title: String? = null
    private var subCategoryID: Int = 0


    companion object{
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
            bundle.putString("title", "TV Channels")
            channelListFragment.arguments = bundle
            return channelListFragment
        }

        fun createInstance(category: String): ChannelFragment {
            val bundle = Bundle()
            val instance = ChannelFragment()
            bundle.putString("category", category)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.title = arguments!!.getString("title")
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
        title?.let {
            activity?.title = it
        }
        var channelAdapter: ChannelStickyListAdapter?
        val gridView: RecyclerView  = view.findViewById(R.id.gridView)
        gridView.setHasFixedSize(true)
        val layoutManager = StickyHeaderGridLayoutManager(3)
        layoutManager.setHeaderBottomOverlapMargin(resources.getDimensionPixelSize(R.dimen.header_shadow_size))
        gridView.layoutManager = layoutManager

        //we will observe channel live data from home activity
        homeViewModel.channelLiveData.observe(viewLifecycleOwner, Observer {
          when(it){
              is Resource.Success->{
                  channelAdapter = ChannelStickyListAdapter(
                      requireContext(),
                      it.data.toMutableList(),
                      this
                  )
                  gridView.adapter = channelAdapter
              }
              is Resource.Failure->{
                  context?.showToast(it.error.msg)
              }
          }
        })
    }
}