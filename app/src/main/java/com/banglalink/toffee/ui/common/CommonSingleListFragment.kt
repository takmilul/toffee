package com.banglalink.toffee.ui.common

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentCatchupBinding
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.MyPopupWindow
import com.banglalink.toffee.ui.home.OptionCallBack
import com.banglalink.toffee.model.ChannelInfo

abstract class CommonSingleListFragment:Fragment(), OptionCallBack {
    val homeViewModel by lazy {
        ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
    }

    protected val baseViewModel by lazy {
        ViewModelProviders.of(this).get(BaseViewModel::class.java)
    }

    var mAdapter: CommonChannelAdapter?=null
    lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private var title: String? = null

    lateinit var binding: FragmentCatchupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadItems(0)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_catchup,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeFavoriteLiveData()

        title = arguments?.getString("title")
        activity?.title = title
        mAdapter= CommonChannelAdapter(this) {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }
        val linearLayoutManager = LinearLayoutManager(context)
        binding.listview.layoutManager = linearLayoutManager
        binding.listview.adapter = mAdapter
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                binding.progressBar.visibility =View.VISIBLE
                loadItems(mAdapter?.getOffset()?:0)
            }
        }
        // Adds the scroll listener to RecyclerView
        binding.listview.addOnScrollListener(scrollListener)
        binding.progress.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    abstract fun loadItems(offset:Int)

    protected open fun observeFavoriteLiveData(){
        baseViewModel.favoriteLiveData.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Success->{
                    val channelInfo = it.data
                    when(channelInfo.favorite){
                        "0"->{
                            context?.showToast("Content successfully removed from favorite list")
                            onFavoriteItemRemoved(channelInfo)
                        }
                        "1"->context?.showToast("Content successfully added to favorite list")
                    }
                }
                is Resource.Failure->{
                    context?.showToast(it.error.msg)
                }
            }
        })
    }


    override fun onOptionClicked(anchor: View, channelInfo: ChannelInfo) {
        val popupMenu = MyPopupWindow(context!!, anchor)
        popupMenu.inflate(R.menu.menu_catchup_item)

        if (channelInfo.favorite == null || channelInfo.favorite == "0") {
            popupMenu.menu.getItem(0).title = "Add to Favorites"
        } else {
            popupMenu.menu.getItem(0).title = "Remove from Favorites"
        }
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
                    mAdapter?.remove(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                else->{
                    return@setOnMenuItemClickListener false
                }
            }
        }
        popupMenu.show()
    }

    override fun viewAllVideoClick() {
        homeViewModel.viewAllVideoLiveData.postValue(true)
    }

    abstract fun onFavoriteItemRemoved(channelInfo: ChannelInfo)

    fun hideProgress(){
        binding.progressBar.visibility = View.GONE
        binding.progress.visibility = View.GONE
    }
}