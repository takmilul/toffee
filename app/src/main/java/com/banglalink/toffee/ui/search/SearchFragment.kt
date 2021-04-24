package com.banglalink.toffee.ui.search

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.report.ReportPopupFragment
import com.banglalink.toffee.ui.widget.MyPopupWindow
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment: BaseListFragment<ChannelInfo>(), ProviderIconCallback<ChannelInfo> {
    
    private lateinit var searchKey: String
    override val itemMargin: Int = 12
    override val verticalPadding = Pair(16, 16)
    @Inject lateinit var factory: SearchViewModel.AssistedFactory
    private val homeViewModel by activityViewModels<HomeViewModel> ()
    override val mAdapter by lazy { SearchAdapter(this) }
    override val mViewModel by viewModels<SearchViewModel> {
        SearchViewModel.provideFactory(factory, searchKey)
    }

    companion object{
        const val SEARCH = "_search_"
        fun createInstance(search: String): SearchFragment {
            val searchListFragment = SearchFragment()
            val bundle = Bundle()
            bundle.putString(SEARCH, search)
            searchListFragment.arguments = bundle
            return searchListFragment
        }
    }

    fun getSearchString(): String? {
        return if(::searchKey.isInitialized) searchKey else null
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        searchKey = arguments?.getString(SEARCH, "")!!
        super.onCreate(savedInstanceState)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Search"
    }

    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
    }

    override fun onOpenMenu(view: View, item: ChannelInfo) {
        openMenu(view, item)
    }
    
    private fun openMenu(anchor: View, channelInfo: ChannelInfo) {
        val popupMenu = MyPopupWindow(requireContext(), anchor)
        popupMenu.inflate(R.menu.menu_catchup_item)

        if (channelInfo.favorite == null || channelInfo.favorite == "0") {
            popupMenu.menu.getItem(0).title = "Add to Favorites"
        }
        else {
            popupMenu.menu.getItem(0).title = "Remove from Favorites"
        }

        popupMenu.menu.findItem(R.id.menu_share).isVisible = true
        popupMenu.setOnMenuItemClickListener{
            when(it?.itemId){
                R.id.menu_share->{
                    if (!mPref.isVerifiedUser) {
                        (activity as HomeActivity).handleVerficationApp()
                        return@setOnMenuItemClickListener true
                    }
                    homeViewModel.shareContentLiveData.postValue(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav->{
                    if (!mPref.isVerifiedUser) {
                        (activity as HomeActivity).handleVerficationApp()
                        return@setOnMenuItemClickListener true
                    }
                    homeViewModel.updateFavorite(channelInfo).observe(viewLifecycleOwner, { resp->
                        handleFavoriteResponse(resp)
                    })
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_report -> {
                    if (!mPref.isVerifiedUser) {
                        (activity as HomeActivity).handleVerficationApp()
                        return@setOnMenuItemClickListener true
                    }
                    val fragment =
                        channelInfo.duration?.let { durations ->
                            ReportPopupFragment.newInstance(-1,
                                durations, channelInfo.id
                            )
                        }
                    fragment?.show(requireActivity().supportFragmentManager, "report_video")
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_not_interested->{
//                    removeItemNotInterestedItem(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                else->{
                    return@setOnMenuItemClickListener false
                }
            }
        }
        popupMenu.show()
    }
    
    private fun handleFavoriteResponse(it: Resource<ChannelInfo>){
        when(it){
            is Success ->{
                val channelInfo = it.data
                when(channelInfo.favorite){
                    "0"->{
                        context?.showToast("Content successfully removed from favorite list")
                    }
                    "1"->{
                        context?.showToast("Content successfully added to favorite list")
                    }
                }
            }
            is Failure ->{
                context?.showToast(it.error.msg)
            }
        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        activity?.let {
//            if(it is HomeActivity){
//                it.closeSearchBarIfOpen()
//            }
//        }
//    }
}