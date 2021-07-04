package com.banglalink.toffee.ui.search

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.mychannel.MyChannelAddToPlaylistFragment
import com.banglalink.toffee.ui.widget.MyPopupWindow
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment: BaseListFragment<ChannelInfo>(), ProviderIconCallback<ChannelInfo> {
    
    private lateinit var searchKey: String
    override val itemMargin: Int = 12
    override val verticalPadding = Pair(16, 16)
    @Inject lateinit var factory: SearchViewModel.AssistedFactory
    @Inject lateinit var favoriteDao: FavoriteItemDao
    private val homeViewModel by activityViewModels<HomeViewModel> ()
    override val mAdapter by lazy { SearchAdapter(this) }
    override val mViewModel by viewModels<SearchViewModel> {
        SearchViewModel.provideFactory(factory, searchKey)
    }

    companion object{
        const val SEARCH_KEYWORD = "keyword"
        fun createInstance(search: String): SearchFragment {
            val searchListFragment = SearchFragment()
            val bundle = Bundle()
            bundle.putString(SEARCH_KEYWORD, search)
            searchListFragment.arguments = bundle
            return searchListFragment
        }
    }

    fun getSearchString(): String? {
        return if(::searchKey.isInitialized) searchKey else null
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        searchKey = arguments?.getString(SEARCH_KEYWORD, "") ?: ""
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
                    requireActivity().handleShare(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_add_to_playlist->{
                    val isUserPlaylist = if (mPref.customerId==channelInfo.channel_owner_id) 0 else 1
                    val fragment = MyChannelAddToPlaylistFragment.newInstance(mPref.customerId, channelInfo, isUserPlaylist)
                    fragment.show(requireActivity().supportFragmentManager, "add_to_playlist")
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav->{
                    requireActivity().handleFavorite(channelInfo, favoriteDao)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_report -> {
                    requireActivity().handleReport(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                else->{
                    return@setOnMenuItemClickListener false
                }
            }
        }
        popupMenu.show()
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