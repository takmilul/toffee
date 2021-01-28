package com.banglalink.toffee.ui.favorite

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.MyPopupWindow

class FavoriteFragment : BaseListFragment<ChannelInfo>(), ProviderIconCallback<ChannelInfo> {

    override val itemMargin: Int = 12
    override val verticalPadding = Pair(16, 16)
    override val mAdapter by lazy { FavoriteAdapter(this) }
    override val mViewModel by viewModels<FavoriteViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Favorites"
    }

    override fun onItemClicked(item: ChannelInfo) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.id.toInt(), item.channel_owner_id, item.isSubscribed)
    }

    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        openMenu(view, item)
    }
    
    fun openMenu(anchor: View, channelInfo: ChannelInfo) {
        val popupMenu = MyPopupWindow(requireContext(), anchor)
        popupMenu.inflate(R.menu.menu_catchup_item)

        if (channelInfo.favorite == null || channelInfo.favorite == "0") {
            popupMenu.menu.getItem(0).title = "Add to Favorites"
        } else {
            popupMenu.menu.getItem(0).title = "Remove from Favorites"
        }
        if(hideNotInterestedMenuItem(channelInfo)){//we are checking if that could be shown or not
            popupMenu.menu.getItem(2).isVisible = false
        }
        popupMenu.setOnMenuItemClickListener{
            when(it?.itemId){
                R.id.menu_share->{
                    homeViewModel.shareContentLiveData.postValue(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav->{
                    homeViewModel.updateFavorite(channelInfo).observe(viewLifecycleOwner, Observer {
                        handleFavoriteResponse(it)
                    })
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

    //hook for subclass for to hide Not Interested Menu Item.
    //In catchup details fragment we need to hide this menu from popup menu for first item
    fun hideNotInterestedMenuItem(channelInfo: ChannelInfo):Boolean{
        return false
    }

    fun handleFavoriteResponse(it: Resource<ChannelInfo>){
        when(it){
            is Resource.Success->{
                val channelInfo = it.data
                when(channelInfo.favorite){
                    "0"->{
                        mAdapter.refresh()
                        context?.showToast("Content successfully removed from favorite list")
//                        handleFavoriteRemovedSuccessFully(channelInfo)
                    }
                    "1"->{
//                        handleFavoriteAddedSuccessfully(channelInfo)
                        context?.showToast("Content successfully added to favorite list")
                    }
                }
            }
            is Resource.Failure->{
                context?.showToast(it.error.msg)
            }
        }
    }

    /*open fun handleFavoriteAddedSuccessfully(channelInfo: ChannelInfo){
        //subclass can hook here
    }

    open fun handleFavoriteRemovedSuccessFully(channelInfo: ChannelInfo){
        //subclass can hook here
    }

    abstract fun removeItemNotInterestedItem(channelInfo: ChannelInfo)*/
}