package com.banglalink.toffee.ui.home

import android.content.Intent
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
import com.banglalink.toffee.ui.widget.MyPopupWindow

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
    lateinit var mAdapter: CatchUpDetailsAdapter
    private var currentItem: ChannelInfo? = null

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
        mAdapter = CatchUpDetailsAdapter(this) {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }
        val listView:RecyclerView = view.findViewById(R.id.listview)
        val progressBar = view.findViewById(R.id.progress_bar) as ProgressBar
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
        if(currentItem!=null)
            mAdapter.add(currentItem!!)//Fake item for enabling header...because we are adding header at 0
        progressBar.visibility = View.VISIBLE
    }

    private fun getContents(offset:Int){
        viewModel.getContents(currentItem!!,offset)

    }
//overriding parent's option click because we want to hide last option menu from the options
    override fun onOptionClicked(anchor: View, channelInfo: ChannelInfo) {
        val popupMenu = MyPopupWindow(context!!, anchor)
        popupMenu.inflate(R.menu.menu_catchup_item)

        if (channelInfo.favorite == null || channelInfo.favorite == "0") {
            popupMenu.menu.getItem(0).title = "Add to Favorites"
        } else {
            popupMenu.menu.getItem(0).title = "Remove from Favorites"
        }
        popupMenu.menu.getItem(2).isVisible = false//hide it
        popupMenu.setOnMenuItemClickListener{
            when(it?.itemId){
                R.id.menu_share->{
                    val sharingIntent = Intent(Intent.ACTION_SEND)
                    sharingIntent.type = "text/plain"
                    sharingIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        channelInfo.video_share_url
                    )
                    activity?.startActivity(Intent.createChooser(sharingIntent, "Share via"))
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav->{
                    baseViewModel.updateFavorite(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_not_interested->{
                    removeItemNotInterestedItem(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                else->{
                    return@setOnMenuItemClickListener false
                }
            }
        }
        popupMenu.show()
    }
}